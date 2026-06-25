package com.example.relatorios

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

data class AppVersionInfo(
    val versionCode: Int = 0,
    val versionName: String = "",
    val downloadUrl: String = ""
)

// Consulta o Firestore em config/appVersion e chama onUpdateAvailable se houver versão maior.
// Estrutura esperada do documento:
//   versionCode: number (ex: 8)
//   versionName: string (ex: "1.7")
//   downloadUrl: string (URL direto do APK)
fun checkForUpdate(currentVersionCode: Int, onUpdateAvailable: (AppVersionInfo) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("config")
        .document("appVersion")
        .get()
        .addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val latestCode = (doc.getLong("versionCode") ?: 0L).toInt()
                val versionName = doc.getString("versionName") ?: ""
                val downloadUrl = doc.getString("downloadUrl") ?: ""
                if (latestCode > currentVersionCode && downloadUrl.isNotEmpty()) {
                    onUpdateAvailable(AppVersionInfo(latestCode, versionName, downloadUrl))
                }
            }
        }
        .addOnFailureListener { e ->
            Log.w("UpdateChecker", "Falha ao verificar atualização: ${e.message}")
        }
}

fun startApkDownload(context: Context, downloadUrl: String): Long {
    val fileName = "relatorios_update.apk"
    val existingFile = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        fileName
    )
    if (existingFile.exists()) existingFile.delete()

    val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
        setTitle("Relatórios — Atualização")
        setDescription("Baixando nova versão...")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        setMimeType("application/vnd.android.package-archive")
    }
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    return dm.enqueue(request)
}

fun installDownloadedApk(context: Context) {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "relatorios_update.apk"
    )
    if (!file.exists()) return
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } else {
        @Suppress("DEPRECATION")
        Uri.fromFile(file)
    }
    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
