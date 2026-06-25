package com.example.relatorios

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.util.concurrent.TimeUnit

data class AppVersionInfo(
    val versionCode: Int = 0,
    val versionName: String = "",
    val releaseTag: String = ""
)

// Firestore: config/appVersion com campos versionCode, versionName, releaseTag (ex: "v1.7")
fun checkForUpdate(currentVersionCode: Int, onUpdateAvailable: (AppVersionInfo) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("config")
        .document("appVersion")
        .get()
        .addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val latestCode = (doc.getLong("versionCode") ?: 0L).toInt()
                val versionName = doc.getString("versionName") ?: ""
                val releaseTag = doc.getString("releaseTag") ?: ""
                if (latestCode > currentVersionCode && releaseTag.isNotEmpty()) {
                    onUpdateAvailable(AppVersionInfo(latestCode, versionName, releaseTag))
                }
            }
        }
        .addOnFailureListener { e ->
            Log.w("UpdateChecker", "Falha ao verificar atualização: ${e.message}")
        }
}

// Busca a URL do APK na release do GitHub e baixa diretamente
suspend fun downloadApk(
    context: Context,
    releaseTag: String,
    onProgress: (Float) -> Unit
): Boolean = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        // Passo 1: busca os assets da release para achar o APK
        val apiUrl = "https://api.github.com/repos/lucascorporativox-lab/appvistoria/releases/tags/$releaseTag"
        val apiRequest = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/vnd.github.v3+json")
            .build()
        val apiResponse = client.newCall(apiRequest).execute()
        if (!apiResponse.isSuccessful) {
            Log.e("UpdateChecker", "API GitHub retornou ${apiResponse.code}")
            return@withContext false
        }

        val json = org.json.JSONObject(apiResponse.body?.string() ?: "")
        val assets: JSONArray = json.getJSONArray("assets")
        var assetApiUrl: String? = null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            if (asset.getString("name").endsWith(".apk")) {
                assetApiUrl = asset.getString("url") // URL da API, não browser_download_url
                break
            }
        }

        if (assetApiUrl == null) {
            Log.e("UpdateChecker", "Nenhum APK encontrado na release $releaseTag")
            return@withContext false
        }
        Log.d("UpdateChecker", "Asset URL: $assetApiUrl")

        // Passo 2: pede o CDN URL via Accept: octet-stream
        val redirectRequest = Request.Builder()
            .url(assetApiUrl)
            .header("Accept", "application/octet-stream")
            .build()
        val redirectResponse = client.newCall(redirectRequest).execute()
        Log.d("UpdateChecker", "Passo 2 — código: ${redirectResponse.code}, location: ${redirectResponse.header("Location")}")

        val cdnUrl = redirectResponse.header("Location") ?: run {
            if (redirectResponse.isSuccessful) {
                return@withContext streamToFile(context, redirectResponse, onProgress)
            }
            Log.e("UpdateChecker", "Sem CDN URL, código: ${redirectResponse.code}")
            return@withContext false
        }
        redirectResponse.close()

        // Passo 3: baixa do CDN
        val downloadResponse = client.newCall(
            Request.Builder().url(cdnUrl).build()
        ).execute()
        Log.d("UpdateChecker", "Passo 3 — código: ${downloadResponse.code}, tamanho: ${downloadResponse.header("Content-Length")}")

        if (!downloadResponse.isSuccessful) {
            Log.e("UpdateChecker", "CDN retornou ${downloadResponse.code}")
            return@withContext false
        }

        streamToFile(context, downloadResponse, onProgress)
    } catch (e: Exception) {
        Log.e("UpdateChecker", "Erro no download: ${e.message}", e)
        false
    }
}

private suspend fun streamToFile(
    context: Context,
    response: okhttp3.Response,
    onProgress: (Float) -> Unit
): Boolean {
    val contentType = response.header("Content-Type") ?: ""
    if (contentType.contains("html", ignoreCase = true)) {
        Log.e("UpdateChecker", "Resposta é HTML: $contentType")
        response.close()
        return false
    }
    val body = response.body ?: return false
    val contentLength = body.contentLength()
    val file = File(context.getExternalFilesDir(null), "relatorios_update.apk")
    if (file.exists()) file.delete()

    body.byteStream().use { input ->
        file.outputStream().use { output ->
            val buffer = ByteArray(8192)
            var bytesRead = 0L
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                bytesRead += read
                if (contentLength > 0) {
                    withContext(Dispatchers.Main) {
                        onProgress(bytesRead.toFloat() / contentLength.toFloat())
                    }
                }
            }
        }
    }
    Log.d("UpdateChecker", "APK salvo: ${file.length()} bytes")
    return file.length() > 1_000_000
}

fun installDownloadedApk(context: Context) {
    val file = File(context.getExternalFilesDir(null), "relatorios_update.apk")
    if (!file.exists()) {
        Log.e("UpdateChecker", "APK não encontrado: ${file.absolutePath}")
        return
    }
    Log.d("UpdateChecker", "Instalando: ${file.absolutePath} (${file.length()} bytes)")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
