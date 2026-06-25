package com.example.relatorios

import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri as AndroidUri
import android.os.Build as AndroidBuild
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import com.example.relatorios.data.model.Edificio
import com.example.relatorios.data.model.Report
import com.example.relatorios.ui.screens.*
import com.example.relatorios.ui.theme.RelatoriosTheme
import com.example.relatorios.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import java.io.FileOutputStream as JavaFileOutputStream
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.content.Context as AndroidContext
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Image as ITextImage
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.io.image.ImageDataFactory
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment as ITextVerticalAlignment
import android.os.Environment as AndroidEnvironment
import androidx.core.content.FileProvider
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.Font
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Checkbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardDefaults
import org.json.JSONObject
import java.net.URL
import java.io.BufferedInputStream
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip

enum class SortOrder(val label: String) {
    DATA_DESC("Mais recente"),
    DATA_ASC("Mais antiga"),
    NOME("Nome (A-Z)"),
    BAIRRO("Bairro (A-Z)")
}

class CepVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val out = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 5) append('-')
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 5) offset else (offset + 1).coerceAtMost(out.length)
            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 5) offset else (offset - 1).coerceAtLeast(0)
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

data class ViaCepResponse(
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String
)

suspend fun fetchViaCep(cep: String): ViaCepResponse? = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://viacep.com.br/ws/$cep/json/")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext null
        val json = JSONObject(body)
        if (json.has("erro")) return@withContext null
        ViaCepResponse(
            logradouro = json.optString("logradouro", ""),
            bairro     = json.optString("bairro", ""),
            localidade = json.optString("localidade", ""),
            uf         = json.optString("uf", "")
        )
    } catch (e: Exception) {
        null
    }
}

// Lista de bairros no topo do arquivo, após os imports
val bairrosList = listOf(
    "Afogados", "Aflitos", "Bela Vista", "Bongi", "Boa Viagem", "Boa Vista", "Campo Grande", "Casa Amarela", "Casa Forte", "Caxangá", "Cidade Universitária", "Cordeiro", "Encruzilhada", "Espinheiro", "Fundão", "Graças", "Gracas", "Ilha do Leite", "Ilha do Retiro", "Imbiribeira", "Iputinga", "Jardim Primavera", "Jardim São Paulo", "Madalena", "Monteiro", "Morro da Conceição", "Parnamirim", "Pina", "Poço", "Prado", "Primavera", "Rosarinho", "Santana", "Santo Amaro", "São José", "Soledade", "Sítio dos Pintos", "Tamarineira", "Tejipió", "Torre", "Torreão", "Várzea", "Vila Inabi"
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Solicitar permissões necessárias
        if (AndroidBuild.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.R) {
            if (!AndroidEnvironment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = AndroidUri.parse("package:${applicationContext.packageName}")
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivity(intent)
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }

        // Verificar atualizações
        

        setContent {
            RelatoriosTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Background com imagem específica
                    Image(
                        painter = painterResource(id = R.drawable.fundo),
                        contentDescription = "Imagem de fundo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                        alpha = 0.3f
                    )
                    
                    // Conteúdo do app com fundo semi-transparente
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                    ) {
                        MainScreen()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Verificação de atualização desativada
        // if (requestCode == 1) {
        //     if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //         
        //     }
        // }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: ReportViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }

    // ── Auto-update ─────────────────────────────────────────────
    var updateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        checkForUpdate(BuildConfig.VERSION_CODE) { info ->
            updateInfo = info
        }
    }

    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { if (!isDownloading) updateInfo = null },
            containerColor = Color.White,
            titleContentColor = Color(0xFF1A2B4A),
            textContentColor = Color(0xFF4A5568),
            title = { Text("Atualização disponível", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nova versão ${info.versionName} disponível. Deseja atualizar agora?")
                    if (isDownloading) {
                        LinearProgressIndicator(
                            progress = downloadProgress,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF2D7DD2)
                        )
                        Text(
                            if (downloadProgress > 0f) "${(downloadProgress * 100).toInt()}%" else "Conectando...",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7A99)
                        )
                    }
                }
            },
            confirmButton = {
                if (!isDownloading) {
                    Button(
                        onClick = {
                            isDownloading = true
                            downloadProgress = 0f
                            scope.launch {
                                val success = downloadApk(context, info.releaseTag) { progress ->
                                    downloadProgress = progress
                                }
                                isDownloading = false
                                updateInfo = null
                                if (success) {
                                    installDownloadedApk(context)
                                } else {
                                    Toast.makeText(context, "Falha ao baixar atualização", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D7DD2))
                    ) {
                        Text("Atualizar")
                    }
                }
            },
            dismissButton = {
                if (!isDownloading) {
                    TextButton(onClick = { updateInfo = null }) {
                        Text("Agora não", color = Color(0xFF6B7A99))
                    }
                }
            }
        )
    }
    // ── fim auto-update ──────────────────────────────────────────

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFEEF0F5)) {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                when (selectedTab) {
                    0 -> ReportListScreen(navController, viewModel, onNewReport = { selectedTab = 1 })
                    1 -> NewReportScreen(navController, viewModel, { selectedTab = it })
                }
            }
            composable("edit_report/{reportId}") { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                EditReportScreen(navController, viewModel, reportId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    navController: androidx.navigation.NavController,
    viewModel: ReportViewModel,
    onNewReport: () -> Unit
) {
    val reports by viewModel.reports.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBairro by remember { mutableStateOf("") }
    var expandedBairro by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reportToDelete by remember { mutableStateOf<Report?>(null) }
    var sortOrder by remember { mutableStateOf(SortOrder.DATA_DESC) }
    var expandedSort by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            viewModel.loadReports()
            isLoading = false
        } catch (e: Exception) {
            Log.e("ReportListScreen", "Erro ao carregar relatórios", e)
            Toast.makeText(context, "Erro ao carregar relatórios: ${e.message}", Toast.LENGTH_LONG).show()
            isLoading = false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; reportToDelete = null },
            containerColor = Color.White,
            titleContentColor = Color(0xFF1A2B4A),
            textContentColor = Color(0xFF4A5568),
            title = { Text("Confirmar Exclusão", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir o relatório de ${reportToDelete?.nomeEdificio}?") },
            confirmButton = {
                TextButton(onClick = {
                    reportToDelete?.let { viewModel.deleteReport(it.id) }
                    showDeleteDialog = false; reportToDelete = null
                }) { Text("Excluir", color = Color(0xFFC0392B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; reportToDelete = null }) {
                    Text("Cancelar", color = Color(0xFF6B7A99))
                }
            }
        )
    }

    val filteredReports = reports.filter { report ->
        val matchesSearch = searchQuery.isBlank() ||
            report.nomeEdificio.contains(searchQuery, ignoreCase = true) ||
            report.endereco.contains(searchQuery, ignoreCase = true)
        val bairro = report.endereco.split(",").getOrNull(1)?.trim() ?: ""
        val matchesBairro = selectedBairro.isBlank() || bairro.equals(selectedBairro, ignoreCase = true)
        matchesSearch && matchesBairro
    }.let { list ->
        when (sortOrder) {
            SortOrder.DATA_DESC -> list.sortedByDescending { it.date }
            SortOrder.DATA_ASC  -> list.sortedBy { it.date }
            SortOrder.NOME      -> list.sortedBy { it.nomeEdificio.lowercase() }
            SortOrder.BAIRRO    -> list.sortedBy { it.endereco.split(",").getOrNull(1)?.trim()?.lowercase() ?: "" }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header navy ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .background(Color(0xFF1A2B4A))
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_myconnect),
                    contentDescription = "Logo MY CONNECT",
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentScale = ContentScale.Fit
                )
                Button(
                    onClick = onNewReport,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D7DD2)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nova", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Barra de pesquisa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF243556), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF90A4BE), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar", color = Color(0xFF90A4BE), fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    textStyle = ComposeTextStyle(fontSize = 13.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2D7DD2))
            }
        } else {
            // ── Lista ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFEEF0F5))
            ) {
                val count = filteredReports.size
                Text(
                    text = "$count vistoria${if (count != 1) "s" else ""} encontrada${if (count != 1) "s" else ""}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7A99),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                if (filteredReports.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhuma vistoria encontrada", color = Color(0xFF8896B0), fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredReports, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                onEdit = { navController.navigate("edit_report/${report.id}") },
                                onDelete = { reportToDelete = report; showDeleteDialog = true },
                                onPdf = {
                                    try { generatePdf(context, report) }
                                    catch (e: Exception) { Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show() }
                                },
                                onExcel = {
                                    try { generateExcel(context, listOf(report)) }
                                    catch (e: Exception) { Toast.makeText(context, "Erro ao gerar Excel: ${e.message}", Toast.LENGTH_LONG).show() }
                                }
                            )
                        }
                    }
                }
            }

            // ── Footer ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .background(Color.White)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedBairro,
                    onExpandedChange = { expandedBairro = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedBairro,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Filtrar por bairro", fontSize = 12.sp, color = Color(0xFF6B7A99)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBairro) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F6FA),
                            focusedContainerColor = Color(0xFFF5F6FA),
                            unfocusedBorderColor = Color(0xFFC8D0E0),
                            focusedBorderColor = Color(0xFF2D7DD2),
                            unfocusedTextColor = Color(0xFF1A2B4A),
                            focusedTextColor = Color(0xFF1A2B4A)
                        ),
                        textStyle = ComposeTextStyle(fontSize = 12.sp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBairro,
                        onDismissRequest = { expandedBairro = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos os bairros", color = Color(0xFF1A2B4A), fontSize = 13.sp) },
                            onClick = { selectedBairro = ""; expandedBairro = false }
                        )
                        bairrosList.forEach { bairro ->
                            DropdownMenuItem(
                                text = { Text(bairro, color = Color(0xFF1A2B4A), fontSize = 13.sp) },
                                onClick = { selectedBairro = bairro; expandedBairro = false }
                            )
                        }
                    }
                }
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (sortOrder != SortOrder.DATA_DESC) Color(0xFFEEF3FB) else Color(0xFFF5F6FA),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { expandedSort = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Ordenar",
                            tint = if (sortOrder != SortOrder.DATA_DESC) Color(0xFF2D7DD2) else Color(0xFF6B7A99),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = { expandedSort = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        SortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (sortOrder == order)
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2D7DD2), modifier = Modifier.size(14.dp))
                                        else
                                            Spacer(modifier = Modifier.width(14.dp))
                                        Text(order.label, color = Color(0xFF1A2B4A), fontSize = 13.sp)
                                    }
                                },
                                onClick = { sortOrder = order; expandedSort = false }
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF5F6FA), RoundedCornerShape(8.dp))
                        .clickable {
                            try { generateExcel(context, filteredReports) }
                            catch (e: Exception) { Toast.makeText(context, "Erro ao gerar Excel: ${e.message}", Toast.LENGTH_LONG).show() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = "Exportar Excel", tint = Color(0xFF1E7E45), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    report: Report,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPdf: () -> Unit,
    onExcel: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale("pt", "BR")) }
    val bairro = report.endereco.split(",").getOrNull(1)?.trim() ?: ""
    val dateStr = remember(report.date) { dateFormat.format(report.date) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFD5DAE6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Linha principal: ícone + nome + bairro/data
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEEF3FB), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF2D7DD2), modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = report.nomeEdificio,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A2B4A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (report.aprovado != null) {
                        val badgeBg = if (report.aprovado == true) Color(0xFFE8F5EE) else Color(0xFFFDF0EE)
                        val badgeText = if (report.aprovado == true) "Aprovado" else "Reprovado"
                        val badgeColor = if (report.aprovado == true) Color(0xFF1E7E45) else Color(0xFFC0392B)
                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = badgeColor)
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF8896B0), modifier = Modifier.size(11.dp))
                    Text(
                        text = buildString {
                            if (bairro.isNotEmpty()) { append(bairro); append(" · ") }
                            append(dateStr)
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF8896B0)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE8ECF4), thickness = 0.5.dp)

        // Barra de ações
        Row(modifier = Modifier.fillMaxWidth().height(52.dp)) {
            CardActionButton(
                label = "Editar",
                icon = Icons.Default.Edit,
                iconBg = Color(0xFFEEF3FB),
                iconTint = Color(0xFF2D7DD2),
                labelColor = Color(0xFF2D7DD2),
                modifier = Modifier.weight(1f),
                onClick = onEdit
            )
            Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(Color(0xFFE8ECF4)))
            CardActionButton(
                label = "PDF",
                icon = Icons.Default.PictureAsPdf,
                iconBg = Color(0xFFFDF0EE),
                iconTint = Color(0xFFC0392B),
                labelColor = Color(0xFFC0392B),
                modifier = Modifier.weight(1f),
                onClick = onPdf
            )
            Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(Color(0xFFE8ECF4)))
            CardActionButton(
                label = "Excel",
                icon = Icons.Default.TableChart,
                iconBg = Color(0xFFEEF8F1),
                iconTint = Color(0xFF1E7E45),
                labelColor = Color(0xFF1E7E45),
                modifier = Modifier.weight(1f),
                onClick = onExcel
            )
            Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(Color(0xFFE8ECF4)))
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xFFF5F6FA), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFAAB4C8), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun CardActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(iconBg, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = labelColor)
    }
}

@Composable
fun FormSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFD5DAE6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A2B4A)
            )
            HorizontalDivider(color = Color(0xFFE8ECF4), thickness = 0.5.dp)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportItem(
    report: Report,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1722F8),
            disabledContainerColor = Color(0xFF1722F8),
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = report.nomeEdificio,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Editar",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Excluir",
                        tint = Color.White
                    )
                }
                // Botão de exportar PDF
                IconButton(
                    onClick = { 
                        try {
                            generatePdf(context, report)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar PDF",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                // Botão de exportar Excel
                IconButton(
                    onClick = { 
                        try {
                            generateExcel(context, listOf(report))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao gerar Excel: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = "Exportar Excel",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            titleContentColor = Color(0xFF1A2B4A),
            textContentColor = Color(0xFF4A5568),
            title = { Text("Confirmar Exclusão", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir o relatório de ${report.nomeEdificio}?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Excluir", color = Color(0xFFC0392B), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color(0xFF6B7A99))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(
    navController: androidx.navigation.NavController,
    viewModel: ReportViewModel,
    onTabChange: (Int) -> Unit
) {
    // Variável de estado para exibir foto em tamanho maior
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var nomeEdificio by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var andares by remember { mutableStateOf("") }
    var quantidadeBlocos by remember { mutableStateOf("") }
    var expandedBlocos by remember { mutableStateOf(false) }
    var apartamentosPorAndar by remember { mutableStateOf("") }
    var provedores by remember { mutableStateOf("") }
    var responsavelNome by remember { mutableStateOf("") }
    var responsavelVistoriaNome by remember { mutableStateOf("") }
    var nomesProvedores by remember { mutableStateOf<List<String>>(emptyList()) }
    var vagasOcupadasProvedores by remember { mutableStateOf<List<String>>(emptyList()) }
    var andaresProvedores by remember { mutableStateOf<List<String>>(emptyList()) }
    var condicaoShaft by remember { mutableStateOf("") }
    var meioEntrada by remember { mutableStateOf("Aéreo") }
    var tipoInstalacao by remember { mutableStateOf("CTO") }
    var observacoesGerais by remember { mutableStateOf("") }
    var photos by remember { mutableStateOf<List<String>>(emptyList()) }
    var shaftPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var createdReportId by remember { mutableStateOf<String?>(null) }
    val reports by viewModel.reports.collectAsState()
    val createdReport = reports.find { it.id == createdReportId }
    val shaftPhotosList = createdReport?.fotosShaft ?: emptyList()
    var responsavelTelefone by remember { mutableStateOf("") }
    var aprovado by remember { mutableStateOf<Boolean?>(null) }
    var motivoResultado by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var isLoadingCep by remember { mutableStateOf(false) }
    var cepError by remember { mutableStateOf("") }
    var cidadeUf by remember { mutableStateOf("") }
    var expandedBairro by remember { mutableStateOf(false) }
    var expandedAndares by remember { mutableStateOf(false) }
    var expandedAptos by remember { mutableStateOf(false) }
    var expandedProvedores by remember { mutableStateOf(false) }
    var expandedAndaresSelecionados by remember { mutableStateOf(false) }
    var andaresSelecionados by remember { mutableStateOf(setOf<Int>()) }

    val context = LocalContext.current
    val andaresArray = context.resources.getStringArray(R.array.numero_andares)
    val aptosArray = context.resources.getStringArray(R.array.apartamentos_por_andar)
    val provedoresArray = context.resources.getStringArray(R.array.numero_provedores)

    LaunchedEffect(cep) {
        if (cep.length == 8) {
            isLoadingCep = true
            cepError = ""
            val result = fetchViaCep(cep)
            isLoadingCep = false
            if (result != null) {
                endereco = result.logradouro
                bairro = result.bairro
                cidadeUf = "${result.localidade} - ${result.uf}"
            } else {
                cepError = "CEP não encontrado"
                cidadeUf = ""
            }
        } else {
            cidadeUf = ""
            cepError = ""
        }
    }

    // Atualiza o nome do relatório quando o nome do edifício muda
    LaunchedEffect(nomeEdificio) {
        nome = if (nomeEdificio.isNotEmpty()) {
            "Relatório de Vistoria - $nomeEdificio"
        } else {
            ""
        }
    }
    
    // Estado para controlar a exibição do diálogo de escolha de foto geral
    var showGeneralPhotoDialog by remember { mutableStateOf(false) }
    // Estado para controlar a exibição do diálogo de escolha de foto do shaft
    var showShaftPhotoDialog by remember { mutableStateOf(false) }

    // Launcher para selecionar foto geral da galeria
    val pickGeneralPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: AndroidUri? ->
        uri?.let { selectedImageUri ->
            val savedPath = savePhotoToInternalStorage(context, selectedImageUri)
            savedPath?.let { path ->
                val fileToUpload = File(path)
                if (fileToUpload.exists()) {
                    uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                        if (link != null) {
                            photos = photos + link
                        }
                    }
                } else {
                     Log.e("NewReportScreen", "Arquivo local geral não encontrado após salvar: $path")
                }
            } ?: run { 
                Log.e("NewReportScreen", "Falha ao salvar foto geral no armazenamento interno")
            }
        }
    }

     // Variável de estado para o URI da foto da câmera geral
    var cameraGeneralImageUri: AndroidUri? by remember { mutableStateOf(null) }

    // Launcher para tirar foto geral com a câmera
    val takeGeneralPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraGeneralImageUri?.let { uri ->
                 val savedPath = savePhotoToInternalStorage(context, uri)
                 savedPath?.let { path ->
                    val fileToUpload = File(path)
                    if (fileToUpload.exists()) {
                        uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                            if (link != null) {
                                photos = photos + link
                            }
                        }
                    } else {
                        Log.e("NewReportScreen", "Arquivo local da câmera geral não encontrado após salvar: $path")
                    }
                } ?: run { 
                    Log.e("NewReportScreen", "Falha ao salvar foto da câmera geral no armazenamento interno")
                }
            } ?: run { 
                Log.e("NewReportScreen", "URI da câmera geral era nulo após sucesso")
            }
        } else {
            Toast.makeText(context, "Falha ao capturar foto geral.", Toast.LENGTH_SHORT).show()
        }
        cameraGeneralImageUri = null // Limpar o URI após o uso
    }

    // Launcher para selecionar foto do shaft da galeria
    val pickShaftPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: AndroidUri? ->
        uri?.let { selectedImageUri ->
            val savedPath = savePhotoToInternalStorage(context, selectedImageUri)
             savedPath?.let { path ->
                val fileToUpload = File(path)
                 if (fileToUpload.exists()) {
                     uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                        if (link != null) {
                            shaftPhotos = shaftPhotos + link
                        }
                    }
                 } else {
                     Log.e("NewReportScreen", "Arquivo local do shaft não encontrado após salvar: $path")
                 }
            } ?: run { 
                Log.e("NewReportScreen", "Falha ao salvar foto do shaft no armazenamento interno")
            }
        }
    }

    // Variável de estado para o URI da foto da câmera do shaft
    var cameraShaftImageUri: AndroidUri? by remember { mutableStateOf(null) }

    // Launcher para tirar foto do shaft com a câmera
    val takeShaftPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraShaftImageUri?.let { uri ->
                 val savedPath = savePhotoToInternalStorage(context, uri)
                 savedPath?.let { path ->
                    val fileToUpload = File(path)
                    if (fileToUpload.exists()) {
                        uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                            if (link != null) {
                                shaftPhotos = shaftPhotos + link
                            }
                        }
                    } else {
                         Log.e("NewReportScreen", "Arquivo local da câmera do shaft não encontrado após salvar: $path")
                    }
                } ?: run { 
                    Log.e("NewReportScreen", "Falha ao salvar foto da câmera do shaft no armazenamento interno")
                }
            } ?: run { 
                Log.e("NewReportScreen", "URI da câmera do shaft era nulo após sucesso")
            }
        } else {
            Toast.makeText(context, "Falha ao capturar foto do shaft.", Toast.LENGTH_SHORT).show()
        }
        cameraShaftImageUri = null // Limpar o URI após o uso
    }

    // Diálogo de escolha de origem da foto geral
    if (showGeneralPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showGeneralPhotoDialog = false },
            containerColor = Color.White,
            titleContentColor = Color(0xFF1A2B4A),
            textContentColor = Color(0xFF4A5568),
            title = { Text("Selecionar foto", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 8.dp), color = Color(0xFF4A5568))
                    TextButton(onClick = {
                        val photoFile = File(context.cacheDir, "camera_photo_general_${System.currentTimeMillis()}.jpg")
                        cameraGeneralImageUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            photoFile
                        )
                        takeGeneralPictureLauncher.launch(cameraGeneralImageUri)
                        showGeneralPhotoDialog = false
                    }) { Text("Câmera", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = {
                        pickGeneralPhotoLauncher.launch("image/*")
                        showGeneralPhotoDialog = false
                    }) { Text("Galeria", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = { showGeneralPhotoDialog = false }) { Text("Cancelar", color = Color(0xFF6B7A99)) }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Diálogo de escolha de origem da foto do shaft
    if (showShaftPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showShaftPhotoDialog = false },
            containerColor = Color.White,
            titleContentColor = Color(0xFF1A2B4A),
            textContentColor = Color(0xFF4A5568),
            title = { Text("Selecionar foto do Shaft", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 8.dp), color = Color(0xFF4A5568))
                    TextButton(onClick = {
                        val photoFile = File(context.cacheDir, "camera_photo_shaft_${System.currentTimeMillis()}.jpg")
                        cameraShaftImageUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            photoFile
                        )
                        takeShaftPictureLauncher.launch(cameraShaftImageUri)
                        showShaftPhotoDialog = false
                    }) { Text("Câmera", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = {
                        pickShaftPhotoLauncher.launch("image/*")
                        showShaftPhotoDialog = false
                    }) { Text("Galeria", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = { showShaftPhotoDialog = false }) { Text("Cancelar", color = Color(0xFF6B7A99)) }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = Color(0xFFF5F6FA),
        focusedContainerColor  = Color(0xFFF5F6FA),
        unfocusedBorderColor   = Color(0xFFC8D0E0),
        focusedBorderColor     = Color(0xFF2D7DD2),
        unfocusedTextColor     = Color(0xFF1A2B4A),
        focusedTextColor       = Color(0xFF1A2B4A),
        unfocusedLabelColor    = Color(0xFF6B7A99),
        focusedLabelColor      = Color(0xFF2D7DD2),
        cursorColor            = Color(0xFF2D7DD2),
        disabledContainerColor = Color(0xFFF5F6FA),
        disabledBorderColor    = Color(0xFFE8ECF4),
        disabledTextColor      = Color(0xFF8896B0)
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEEF0F5))) {

        // Header navy
        Row(
            modifier = Modifier
                .background(Color(0xFF1A2B4A))
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onTabChange(0) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text("Novo Relatório", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logo_myconnect),
                contentDescription = "Logo MY CONNECT",
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0EE)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFC0392B))
                ) {
                    Text(errorMessage, color = Color(0xFFC0392B), modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                }
                LaunchedEffect(showError) { showError = false }
            }

            // ── Responsável pela Vistoria ───────────────────────────
            FormSection("Responsável pela Vistoria") {
                OutlinedTextField(
                    value = responsavelVistoriaNome, onValueChange = { responsavelVistoriaNome = it },
                    label = { Text("Nome do responsável") },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                )
            }

            // ── Endereço ────────────────────────────────────────────
            FormSection("Endereço") {
                OutlinedTextField(
                    value = cep,
                    onValueChange = { newVal -> val digits = newVal.filter { it.isDigit() }; if (digits.length <= 8) cep = digits },
                    label = { Text("CEP") },
                    placeholder = { Text("00000-000", color = Color(0xFFAAB4C8)) },
                    visualTransformation = CepVisualTransformation(),
                    trailingIcon = { if (isLoadingCep) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF2D7DD2), strokeWidth = 2.dp) },
                    isError = cepError.isNotEmpty(),
                    supportingText = { when { cepError.isNotEmpty() -> Text(cepError, color = MaterialTheme.colorScheme.error); cidadeUf.isNotEmpty() -> Text(cidadeUf, color = Color(0xFF6B7A99)) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors
                )
                OutlinedTextField(
                    value = nomeEdificio, onValueChange = { nomeEdificio = it },
                    label = { Text("Nome do Edifício") },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                )
                OutlinedTextField(
                    value = endereco, onValueChange = { endereco = it },
                    label = { Text("Rua, Número") },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                )
                ExposedDropdownMenuBox(expanded = expandedBairro, onExpandedChange = { expandedBairro = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = bairro, onValueChange = {}, readOnly = true,
                        label = { Text("Bairro") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBairro) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = expandedBairro, onDismissRequest = { expandedBairro = false }, modifier = Modifier.background(Color.White)) {
                        bairrosList.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = { bairro = item; expandedBairro = false }) }
                    }
                }
                if (nome.isNotEmpty()) {
                    OutlinedTextField(
                        value = nome, onValueChange = {}, enabled = false,
                        label = { Text("Nome do Relatório") },
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors
                    )
                }
            }

            // ── Informações do Edifício ─────────────────────────────
            FormSection("Informações do Edifício") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(expanded = expandedAndares, onExpandedChange = { expandedAndares = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = andares, onValueChange = {}, readOnly = true, label = { Text("Andares") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndares) }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors)
                        ExposedDropdownMenu(expanded = expandedAndares, onDismissRequest = { expandedAndares = false }, modifier = Modifier.background(Color.White)) {
                            andaresArray.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = { andares = item; expandedAndares = false }) }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = expandedAptos, onExpandedChange = { expandedAptos = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = apartamentosPorAndar, onValueChange = {}, readOnly = true, label = { Text("Aptos/Andar") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAptos) }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors)
                        ExposedDropdownMenu(expanded = expandedAptos, onDismissRequest = { expandedAptos = false }, modifier = Modifier.background(Color.White)) {
                            aptosArray.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = { apartamentosPorAndar = item; expandedAptos = false }) }
                        }
                    }
                }
                OutlinedTextField(value = quantidadeBlocos, onValueChange = { quantidadeBlocos = it }, label = { Text("Quantidade de Blocos") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true)
                ExposedDropdownMenuBox(expanded = expandedAndaresSelecionados, onExpandedChange = { expandedAndaresSelecionados = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (andaresSelecionados.isEmpty()) "" else andaresSelecionados.sorted().joinToString(", "),
                        onValueChange = {}, readOnly = true,
                        label = { Text("Andares para instalação CTO/Modular") },
                        placeholder = { Text("Selecione os andares", color = Color(0xFFAAB4C8)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndaresSelecionados) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = expandedAndaresSelecionados, onDismissRequest = { expandedAndaresSelecionados = false }, modifier = Modifier.background(Color.White)) {
                        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                val totalAndaresInt = andares.toIntOrNull() ?: 0
                                if (totalAndaresInt > 0) {
                                    for (andar in 1..totalAndaresInt) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .clickable { andaresSelecionados = if (andaresSelecionados.contains(andar)) andaresSelecionados.minus(andar) else andaresSelecionados.plus(andar) }
                                                .padding(vertical = 8.dp, horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(checked = andaresSelecionados.contains(andar), onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2D7DD2), uncheckedColor = Color(0xFF6B7A99)))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Andar $andar", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF1A2B4A))
                                        }
                                    }
                                } else {
                                    Text("Selecione o número de andares primeiro", modifier = Modifier.padding(16.dp), color = Color(0xFF6B7A99))
                                }
                            }
                        }
                    }
                }
            }

            // ── Infraestrutura ──────────────────────────────────────
            FormSection("Infraestrutura") {
                ExposedDropdownMenuBox(expanded = expandedProvedores, onExpandedChange = { expandedProvedores = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = provedores, onValueChange = {}, readOnly = true, label = { Text("Número de Provedores") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvedores) }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors)
                    ExposedDropdownMenu(expanded = expandedProvedores, onDismissRequest = { expandedProvedores = false }, modifier = Modifier.background(Color.White)) {
                        provedoresArray.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = {
                            val n = item.toInt()
                            provedores = item
                            nomesProvedores = List(n) { "" }
                            vagasOcupadasProvedores = List(n) { "" }
                            andaresProvedores = List(n) { "" }
                            expandedProvedores = false
                        }) }
                    }
                }
                if (provedores.isNotEmpty() && (provedores.toIntOrNull() ?: 0) > 0) {
                    repeat(provedores.toInt()) { index ->
                        if (index > 0) HorizontalDivider(color = Color(0xFFE8ECF4), modifier = Modifier.padding(vertical = 4.dp))
                        Text("Provedor ${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7A99))
                        OutlinedTextField(
                            value = nomesProvedores.getOrNull(index) ?: "",
                            onValueChange = { v -> val l = nomesProvedores.toMutableList(); if (index < l.size) l[index] = v else l.add(v); nomesProvedores = l },
                            label = { Text("Nome") },
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vagasOcupadasProvedores.getOrNull(index) ?: "",
                                onValueChange = { v -> val l = vagasOcupadasProvedores.toMutableList(); if (index < l.size) l[index] = v else l.add(v); vagasOcupadasProvedores = l },
                                label = { Text("Vagas ocupadas") },
                                modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = andaresProvedores.getOrNull(index) ?: "",
                                onValueChange = { v -> val l = andaresProvedores.toMutableList(); if (index < l.size) l[index] = v else l.add(v); andaresProvedores = l },
                                label = { Text("Andares das caixas") },
                                modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true
                            )
                        }
                    }
                }
                OutlinedTextField(value = condicaoShaft, onValueChange = { condicaoShaft = it }, label = { Text("Condições do Shaft") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = fieldColors)
                OutlinedButton(onClick = { showShaftPhotoDialog = true }, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Color(0xFF2D7DD2)), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2D7DD2), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar Fotos do Shaft", color = Color(0xFF2D7DD2))
                }
                if (shaftPhotos.isNotEmpty()) {
                    LazyRow(modifier = Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(shaftPhotos) { photoUrl ->
                            Image(painter = rememberAsyncImagePainter(photoUrl), contentDescription = null, modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)).clickable { selectedPhotoUrl = photoUrl }, contentScale = ContentScale.Crop)
                        }
                    }
                }
                OutlinedTextField(value = observacoesGerais, onValueChange = { observacoesGerais = it }, label = { Text("Observações Gerais") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = fieldColors)
            }

            // ── Meio de Entrada ─────────────────────────────────────
            FormSection("Meio de Entrada") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    listOf("Aéreo", "Subterrâneo").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { meioEntrada = option }) {
                            RadioButton(selected = meioEntrada == option, onClick = { meioEntrada = option }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2D7DD2), unselectedColor = Color(0xFF6B7A99)))
                            Text(option, color = Color(0xFF1A2B4A), fontSize = 14.sp)
                        }
                    }
                }
            }

            // ── Tipo de Instalação ──────────────────────────────────
            FormSection("Tipo de Instalação") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    listOf("CTO", "Modular").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tipoInstalacao = option }) {
                            RadioButton(selected = tipoInstalacao == option, onClick = { tipoInstalacao = option }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2D7DD2), unselectedColor = Color(0xFF6B7A99)))
                            Text(option, color = Color(0xFF1A2B4A), fontSize = 14.sp)
                        }
                    }
                }
            }

            // ── Responsável ─────────────────────────────────────────
            FormSection("Responsável") {
                OutlinedTextField(value = responsavelNome, onValueChange = { responsavelNome = it }, label = { Text("Nome do responsável") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true)
                OutlinedTextField(
                    value = responsavelTelefone,
                    onValueChange = { newValue -> val numbers = newValue.filter { it.isDigit() }; if (numbers.length <= 11) responsavelTelefone = numbers },
                    label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    placeholder = { Text("00000000000", color = Color(0xFFAAB4C8)) },
                    colors = fieldColors, singleLine = true
                )
            }

            // ── Resultado da Vistoria ───────────────────────────────
            FormSection("Resultado da Vistoria") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(true to "Aprovado", false to "Reprovado").forEach { (valor, label) ->
                        val selected = aprovado == valor
                        val bgColor = when { selected && valor -> Color(0xFF1E7E45); selected && !valor -> Color(0xFFC0392B); else -> Color(0xFFF5F6FA) }
                        val textColor = if (selected) Color.White else Color(0xFF6B7A99)
                        val borderColor = when { selected && valor -> Color(0xFF1E7E45); selected && !valor -> Color(0xFFC0392B); else -> Color(0xFFC8D0E0) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { aprovado = if (aprovado == valor) null else valor }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                        }
                    }
                }
                OutlinedTextField(
                    value = motivoResultado,
                    onValueChange = { motivoResultado = it },
                    label = { Text(if (aprovado == false) "Motivo da reprovação" else "Justificativa") },
                    placeholder = { Text("Descreva o motivo do resultado...", color = Color(0xFFAAB4C8)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = fieldColors
                )
            }

            // ── Salvar ──────────────────────────────────────────────
            Button(
                onClick = {
                    try {
                        if (nomeEdificio.isEmpty() || endereco.isEmpty() || bairro.isEmpty()) {
                            errorMessage = "Por favor, preencha o nome do edifício, endereço e bairro"
                            showError = true
                            return@Button
                        }
                        val enderecoCompleto = "$endereco, $bairro"
                        val newReport = Report(
                            id = UUID.randomUUID().toString(),
                            nome = nome,
                            endereco = enderecoCompleto,
                            nomeEdificio = nomeEdificio,
                            andares = andares.toIntOrNull() ?: 0,
                            andaresSelecionados = andaresSelecionados.toList(),
                            apartamentosPorAndar = apartamentosPorAndar.toIntOrNull() ?: 0,
                            provedores = provedores.toIntOrNull() ?: 0,
                            nomesProvedores = nomesProvedores,
                            vagasOcupadasProvedores = vagasOcupadasProvedores,
                            andaresProvedores = andaresProvedores,
                            condicaoShaft = condicaoShaft,
                            fotosShaft = shaftPhotos,
                            meioEntrada = meioEntrada,
                            tipoInstalacao = tipoInstalacao,
                            observacoesGerais = observacoesGerais,
                            photos = photos,
                            responsavelNome = responsavelNome,
                            responsavelTelefone = responsavelTelefone,
                            responsavelVistoriaNome = responsavelVistoriaNome,
                            quantidadeBlocos = quantidadeBlocos,
                            cep = cep,
                            date = Date(),
                            aprovado = aprovado,
                            motivoResultado = motivoResultado
                        )
                        viewModel.addReport(newReport)
                        viewModel.setCurrentReport(newReport.id)
                        createdReportId = newReport.id
                        Toast.makeText(context, "Relatório salvo com sucesso", Toast.LENGTH_SHORT).show()
                        onTabChange(0)
                        navController.popBackStack()
                    } catch (e: Exception) {
                        android.util.Log.e("NewReportScreen", "Error creating report: ${e.message}")
                        errorMessage = "Erro ao criar relatório: ${e.message}"
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D7DD2)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Salvar Relatório", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    selectedPhotoUrl?.let { url ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { selectedPhotoUrl = null }, contentAlignment = Alignment.Center) {
            Image(painter = rememberAsyncImagePainter(url), contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.Fit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReportScreen(
    navController: androidx.navigation.NavController,
    viewModel: ReportViewModel,
    reportId: String
) {

    // Variável de estado para exibir foto em tamanho maior
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    Log.d("EditReportScreen", "Iniciando tela de edição para o relatório: $reportId")
    
    val reports by viewModel.reports.collectAsState()
    Log.d("EditReportScreen", "Total de relatórios carregados: ${reports.size}")
    Log.d("EditReportScreen", "reportId recebido: $reportId")
    LaunchedEffect(reportId) {
        viewModel.setCurrentReport(reportId)
    }
    
    val report = reports.find { it.id == reportId }
        ?: run {
            Log.e("EditReportScreen", "Relatório não encontrado: $reportId")
            Toast.makeText(LocalContext.current, "Relatório não encontrado", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            return
        }
    
    Log.d("EditReportScreen", "Relatório encontrado: ${report.nomeEdificio}")
    
    // Inicialização segura dos estados
    var nomeEdificio by remember { mutableStateOf(report.nomeEdificio ?: "") }
    var nome by remember { mutableStateOf(report.nome ?: "") }
    var endereco by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var andares by remember { mutableStateOf((report.andares ?: 0).toString()) }
    var apartamentosPorAndar by remember { mutableStateOf((report.apartamentosPorAndar ?: 0).toString()) }
    var provedores by remember { mutableStateOf((report.provedores ?: 0).toString()) }
    var nomesProvedores by remember { mutableStateOf(report.nomesProvedores ?: emptyList()) }
    var vagasOcupadasProvedores by remember { mutableStateOf(report.vagasOcupadasProvedores) }
    var andaresProvedores by remember { mutableStateOf(report.andaresProvedores) }
    var condicaoShaft by remember { mutableStateOf(report.condicaoShaft ?: "") }
    var meioEntrada by remember { mutableStateOf(report.meioEntrada ?: "Aéreo") }
    var tipoInstalacao by remember { mutableStateOf(report.tipoInstalacao ?: "CTO") }
    var observacoesGerais by remember { mutableStateOf(report.observacoesGerais ?: "") }
    var photos by remember { mutableStateOf(report.photos ?: emptyList()) }
    var shaftPhotos by remember { mutableStateOf(report.fotosShaft ?: emptyList()) }
    var responsavelNome by remember { mutableStateOf(report.responsavelNome ?: "") }
    var responsavelVistoriaNome by remember { mutableStateOf(report.responsavelVistoriaNome ?: "") }
    var responsavelTelefone by remember { mutableStateOf(report.responsavelTelefone ?: "") }
    var quantidadeBlocos by remember { mutableStateOf(report.quantidadeBlocos ?: "") }
    var aprovado by remember { mutableStateOf(report.aprovado) }
    var motivoResultado by remember { mutableStateOf(report.motivoResultado) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf(report.cep ?: "") }
    var isLoadingCep by remember { mutableStateOf(false) }
    var cepError by remember { mutableStateOf("") }
    var cidadeUf by remember { mutableStateOf("") }
    var expandedBairro by remember { mutableStateOf(false) }
    var expandedAndares by remember { mutableStateOf(false) }
    var expandedAptos by remember { mutableStateOf(false) }
    var expandedProvedores by remember { mutableStateOf(false) }
    var expandedAndaresSelecionados by remember { mutableStateOf(false) }
    var andaresSelecionados by remember { mutableStateOf(report.andaresSelecionados.toSet()) }

    val context = LocalContext.current
    val andaresArray = context.resources.getStringArray(R.array.numero_andares)
    val aptosArray = context.resources.getStringArray(R.array.apartamentos_por_andar)
    val provedoresArray = context.resources.getStringArray(R.array.numero_provedores)

    LaunchedEffect(cep) {
        if (cep.length == 8) {
            isLoadingCep = true
            cepError = ""
            val result = fetchViaCep(cep)
            isLoadingCep = false
            if (result != null) {
                endereco = result.logradouro
                bairro = result.bairro
                cidadeUf = "${result.localidade} - ${result.uf}"
            } else {
                cepError = "CEP não encontrado"
                cidadeUf = ""
            }
        } else {
            cidadeUf = ""
            cepError = ""
        }
    }

    // Atualiza o nome do relatório quando o nome do edifício muda
    LaunchedEffect(nomeEdificio) {
        try {
            Log.d("EditReportScreen", "Atualizando nome do relatório para: $nomeEdificio")
            nome = if (nomeEdificio.isNotEmpty()) {
                "Relatório de Vistoria - $nomeEdificio"
            } else {
                ""
            }
            Log.d("EditReportScreen", "Nome do relatório atualizado: $nome")
        } catch (e: Exception) {
            Log.e("EditReportScreen", "Erro ao atualizar nome do relatório: ${e.message}", e)
            errorMessage = "Erro ao atualizar nome do relatório: ${e.message}"
            showError = true
        }
    }
    
    // Extrair endereço e bairro do endereço existente
    LaunchedEffect(report.endereco) {
        try {
            Log.d("EditReportScreen", "Processando endereço: ${report.endereco}")
            val parts = (report.endereco ?: "").split(",")
            if (parts.size > 1) {
                endereco = parts[0].trim()
                bairro = parts[1].trim()
                Log.d("EditReportScreen", "Endereço separado - Rua: $endereco, Bairro: $bairro")
            } else {
                endereco = report.endereco ?: ""
                bairro = ""
                Log.d("EditReportScreen", "Endereço não contém bairro: $endereco")
            }
        } catch (e: Exception) {
            Log.e("EditReportScreen", "Erro ao processar endereço: ${e.message}", e)
            errorMessage = "Erro ao processar endereço: ${e.message}"
            showError = true
            endereco = report.endereco ?: ""
            bairro = ""
        }
    }
    
    // Estado para controlar a exibição do diálogo de escolha de foto geral
    var showGeneralPhotoDialog by remember { mutableStateOf(false) }
    // Estado para controlar a exibição do diálogo de escolha de foto do shaft
    var showShaftPhotoDialog by remember { mutableStateOf(false) }

    // Launcher para selecionar foto geral da galeria
    val pickGeneralPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: AndroidUri? ->
        uri?.let { selectedImageUri ->
            val savedPath = savePhotoToInternalStorage(context, selectedImageUri)
            savedPath?.let { path ->
                val fileToUpload = File(path)
                if (fileToUpload.exists()) {
                    uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                        if (link != null) {
                            photos = photos + link
                        }
                    }
                } else {
                     Log.e("EditReportScreen", "Arquivo local geral não encontrado após salvar: $path")
                }
            } ?: run { 
                Log.e("EditReportScreen", "Falha ao salvar foto geral no armazenamento interno")
            }
        }
    }

     // Variável de estado para o URI da foto da câmera geral
    var cameraGeneralImageUri: AndroidUri? by remember { mutableStateOf(null) }

    // Launcher para tirar foto geral com a câmera
    val takeGeneralPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraGeneralImageUri?.let { uri ->
                 val savedPath = savePhotoToInternalStorage(context, uri)
                 savedPath?.let { path ->
                    val fileToUpload = File(path)
                    if (fileToUpload.exists()) {
                        uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                            if (link != null) {
                                photos = photos + link
                            }
                        }
                    } else {
                        Log.e("EditReportScreen", "Arquivo local da câmera geral não encontrado após salvar: $path")
                    }
                } ?: run { 
                    Log.e("EditReportScreen", "Falha ao salvar foto da câmera geral no armazenamento interno")
                }
            } ?: run { 
                Log.e("EditReportScreen", "URI da câmera geral era nulo após sucesso")
            }
        } else {
            Toast.makeText(context, "Falha ao capturar foto geral.", Toast.LENGTH_SHORT).show()
        }
        cameraGeneralImageUri = null // Limpar o URI após o uso
    }

    // Launcher para selecionar foto do shaft da galeria
    val pickShaftPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: AndroidUri? ->
        uri?.let { selectedImageUri ->
            val savedPath = savePhotoToInternalStorage(context, selectedImageUri)
             savedPath?.let { path ->
                val fileToUpload = File(path)
                 if (fileToUpload.exists()) {
                     uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                        if (link != null) {
                            shaftPhotos = shaftPhotos + link
                        }
                    }
                 } else {
                     Log.e("EditReportScreen", "Arquivo local do shaft não encontrado após salvar: $path")
                 }
            } ?: run { 
                Log.e("EditReportScreen", "Falha ao salvar foto do shaft no armazenamento interno")
            }
        }
    }

    // Variável de estado para o URI da foto da câmera do shaft
    var cameraShaftImageUri: AndroidUri? by remember { mutableStateOf(null) }

    // Launcher para tirar foto do shaft com a câmera
    val takeShaftPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraShaftImageUri?.let { uri ->
                 val savedPath = savePhotoToInternalStorage(context, uri)
                 savedPath?.let { path ->
                    val fileToUpload = File(path)
                    if (fileToUpload.exists()) {
                        uploadImageToImgur(fileToUpload.absolutePath, "9e51efd7a8b3c91") { link ->
                            if (link != null) {
                                shaftPhotos = shaftPhotos + link
                            }
                        }
                    } else {
                         Log.e("EditReportScreen", "Arquivo local da câmera do shaft não encontrado após salvar: $path")
                    }
                } ?: run { 
                    Log.e("EditReportScreen", "Falha ao salvar foto da câmera do shaft no armazenamento interno")
                }
            } ?: run { 
                Log.e("EditReportScreen", "URI da câmera do shaft era nulo após sucesso")
            }
        } else {
            Toast.makeText(context, "Falha ao capturar foto do shaft.", Toast.LENGTH_SHORT).show()
        }
        cameraShaftImageUri = null // Limpar o URI após o uso
    }

    // Diálogo de escolha de origem da foto geral
    if (showGeneralPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showGeneralPhotoDialog = false },
            containerColor = Color.White,
            titleContentColor = Color(0xFF1A2B4A),
            textContentColor = Color(0xFF4A5568),
            title = { Text("Selecionar foto", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 8.dp), color = Color(0xFF4A5568))
                    TextButton(onClick = {
                        val photoFile = File(context.cacheDir, "camera_photo_general_${System.currentTimeMillis()}.jpg")
                        cameraGeneralImageUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            photoFile
                        )
                        takeGeneralPictureLauncher.launch(cameraGeneralImageUri)
                        showGeneralPhotoDialog = false
                    }) { Text("Câmera", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = {
                        pickGeneralPhotoLauncher.launch("image/*")
                        showGeneralPhotoDialog = false
                    }) { Text("Galeria", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = { showGeneralPhotoDialog = false }) { Text("Cancelar", color = Color(0xFF6B7A99)) }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Diálogo de escolha de origem da foto do shaft
    if (showShaftPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showShaftPhotoDialog = false },
            containerColor = Color.White,
            titleContentColor = Color(0xFF1A2B4A),
            textContentColor = Color(0xFF4A5568),
            title = { Text("Selecionar foto do Shaft", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 8.dp), color = Color(0xFF4A5568))
                    TextButton(onClick = {
                        val photoFile = File(context.cacheDir, "camera_photo_shaft_${System.currentTimeMillis()}.jpg")
                        cameraShaftImageUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            photoFile
                        )
                        takeShaftPictureLauncher.launch(cameraShaftImageUri)
                        showShaftPhotoDialog = false
                    }) { Text("Câmera", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = {
                        pickShaftPhotoLauncher.launch("image/*")
                        showShaftPhotoDialog = false
                    }) { Text("Galeria", color = Color(0xFF2D7DD2)) }
                    TextButton(onClick = { showShaftPhotoDialog = false }) { Text("Cancelar", color = Color(0xFF6B7A99)) }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }


    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = Color(0xFFF5F6FA),
        focusedContainerColor  = Color(0xFFF5F6FA),
        unfocusedBorderColor   = Color(0xFFC8D0E0),
        focusedBorderColor     = Color(0xFF2D7DD2),
        unfocusedTextColor     = Color(0xFF1A2B4A),
        focusedTextColor       = Color(0xFF1A2B4A),
        unfocusedLabelColor    = Color(0xFF6B7A99),
        focusedLabelColor      = Color(0xFF2D7DD2),
        cursorColor            = Color(0xFF2D7DD2),
        disabledContainerColor = Color(0xFFF5F6FA),
        disabledBorderColor    = Color(0xFFE8ECF4),
        disabledTextColor      = Color(0xFF8896B0)
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFEEF0F5))) {

        Row(
            modifier = Modifier
                .background(Color(0xFF1A2B4A))
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text("Editar Relatório", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logo_myconnect),
                contentDescription = "Logo MY CONNECT",
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0EE)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFC0392B))
                ) {
                    Text(errorMessage, color = Color(0xFFC0392B), modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                }
                LaunchedEffect(showError) { showError = false }
            }

            FormSection("Responsável pela Vistoria") {
                OutlinedTextField(
                    value = responsavelVistoriaNome, onValueChange = { responsavelVistoriaNome = it },
                    label = { Text("Nome do responsável") },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                )
            }

            FormSection("Endereço") {
                OutlinedTextField(
                    value = cep,
                    onValueChange = { newVal -> val digits = newVal.filter { it.isDigit() }; if (digits.length <= 8) cep = digits },
                    label = { Text("CEP") },
                    placeholder = { Text("00000-000", color = Color(0xFFAAB4C8)) },
                    visualTransformation = CepVisualTransformation(),
                    trailingIcon = { if (isLoadingCep) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF2D7DD2), strokeWidth = 2.dp) },
                    isError = cepError.isNotEmpty(),
                    supportingText = { when { cepError.isNotEmpty() -> Text(cepError, color = MaterialTheme.colorScheme.error); cidadeUf.isNotEmpty() -> Text(cidadeUf, color = Color(0xFF6B7A99)) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors
                )
                OutlinedTextField(
                    value = nomeEdificio, onValueChange = { nomeEdificio = it },
                    label = { Text("Nome do Edifício") },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                )
                OutlinedTextField(
                    value = endereco, onValueChange = { endereco = it },
                    label = { Text("Rua, Número") },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                )
                ExposedDropdownMenuBox(expanded = expandedBairro, onExpandedChange = { expandedBairro = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = bairro, onValueChange = {}, readOnly = true,
                        label = { Text("Bairro") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBairro) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = expandedBairro, onDismissRequest = { expandedBairro = false }, modifier = Modifier.background(Color.White)) {
                        bairrosList.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = { bairro = item; expandedBairro = false }) }
                    }
                }
                if (nome.isNotEmpty()) {
                    OutlinedTextField(
                        value = nome, onValueChange = {}, enabled = false,
                        label = { Text("Nome do Relatório") },
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors
                    )
                }
            }

            FormSection("Informações do Edifício") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(expanded = expandedAndares, onExpandedChange = { expandedAndares = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = andares, onValueChange = {}, readOnly = true, label = { Text("Andares") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndares) }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors)
                        ExposedDropdownMenu(expanded = expandedAndares, onDismissRequest = { expandedAndares = false }, modifier = Modifier.background(Color.White)) {
                            andaresArray.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = { andares = item; expandedAndares = false }) }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = expandedAptos, onExpandedChange = { expandedAptos = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = apartamentosPorAndar, onValueChange = {}, readOnly = true, label = { Text("Aptos/Andar") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAptos) }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors)
                        ExposedDropdownMenu(expanded = expandedAptos, onDismissRequest = { expandedAptos = false }, modifier = Modifier.background(Color.White)) {
                            aptosArray.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = { apartamentosPorAndar = item; expandedAptos = false }) }
                        }
                    }
                }
                OutlinedTextField(value = quantidadeBlocos, onValueChange = { quantidadeBlocos = it }, label = { Text("Quantidade de Blocos") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true)
                ExposedDropdownMenuBox(expanded = expandedAndaresSelecionados, onExpandedChange = { expandedAndaresSelecionados = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (andaresSelecionados.isEmpty()) "" else andaresSelecionados.sorted().joinToString(", "),
                        onValueChange = {}, readOnly = true,
                        label = { Text("Andares para instalação CTO/Modular") },
                        placeholder = { Text("Selecione os andares", color = Color(0xFFAAB4C8)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndaresSelecionados) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = expandedAndaresSelecionados, onDismissRequest = { expandedAndaresSelecionados = false }, modifier = Modifier.background(Color.White)) {
                        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                val totalAndaresInt = andares.toIntOrNull() ?: 0
                                if (totalAndaresInt > 0) {
                                    for (andar in 1..totalAndaresInt) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .clickable { andaresSelecionados = if (andaresSelecionados.contains(andar)) andaresSelecionados.minus(andar) else andaresSelecionados.plus(andar) }
                                                .padding(vertical = 8.dp, horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(checked = andaresSelecionados.contains(andar), onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2D7DD2), uncheckedColor = Color(0xFF6B7A99)))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Andar $andar", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF1A2B4A))
                                        }
                                    }
                                } else {
                                    Text("Selecione o número de andares primeiro", modifier = Modifier.padding(16.dp), color = Color(0xFF6B7A99))
                                }
                            }
                        }
                    }
                }
            }

            FormSection("Infraestrutura") {
                ExposedDropdownMenuBox(expanded = expandedProvedores, onExpandedChange = { expandedProvedores = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = provedores, onValueChange = {}, readOnly = true, label = { Text("Número de Provedores") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvedores) }, modifier = Modifier.fillMaxWidth().menuAnchor(), colors = fieldColors)
                    ExposedDropdownMenu(expanded = expandedProvedores, onDismissRequest = { expandedProvedores = false }, modifier = Modifier.background(Color.White)) {
                        provedoresArray.forEach { item -> DropdownMenuItem(text = { Text(item, color = Color(0xFF1A2B4A)) }, onClick = {
                            val n = item.toInt()
                            provedores = item
                            nomesProvedores = List(n) { i -> nomesProvedores.getOrElse(i) { "" } }
                            vagasOcupadasProvedores = List(n) { i -> vagasOcupadasProvedores.getOrElse(i) { "" } }
                            andaresProvedores = List(n) { i -> andaresProvedores.getOrElse(i) { "" } }
                            expandedProvedores = false
                        }) }
                    }
                }
                if (provedores.isNotEmpty() && (provedores.toIntOrNull() ?: 0) > 0) {
                    repeat(provedores.toInt()) { index ->
                        if (index > 0) HorizontalDivider(color = Color(0xFFE8ECF4), modifier = Modifier.padding(vertical = 4.dp))
                        Text("Provedor ${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7A99))
                        OutlinedTextField(
                            value = nomesProvedores.getOrNull(index) ?: "",
                            onValueChange = { v -> val l = nomesProvedores.toMutableList(); if (index < l.size) l[index] = v else l.add(v); nomesProvedores = l },
                            label = { Text("Nome") },
                            modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vagasOcupadasProvedores.getOrNull(index) ?: "",
                                onValueChange = { v -> val l = vagasOcupadasProvedores.toMutableList(); if (index < l.size) l[index] = v else l.add(v); vagasOcupadasProvedores = l },
                                label = { Text("Vagas ocupadas") },
                                modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = andaresProvedores.getOrNull(index) ?: "",
                                onValueChange = { v -> val l = andaresProvedores.toMutableList(); if (index < l.size) l[index] = v else l.add(v); andaresProvedores = l },
                                label = { Text("Andares das caixas") },
                                modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true
                            )
                        }
                    }
                }
                OutlinedTextField(value = condicaoShaft, onValueChange = { condicaoShaft = it }, label = { Text("Condições do Shaft") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = fieldColors)
                OutlinedButton(onClick = { showShaftPhotoDialog = true }, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Color(0xFF2D7DD2)), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2D7DD2), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar Fotos do Shaft", color = Color(0xFF2D7DD2))
                }
                if (shaftPhotos.isNotEmpty()) {
                    LazyRow(modifier = Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(shaftPhotos) { photoUrl ->
                            Image(painter = rememberAsyncImagePainter(photoUrl), contentDescription = null, modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)).clickable { selectedPhotoUrl = photoUrl }, contentScale = ContentScale.Crop)
                        }
                    }
                }
                OutlinedTextField(value = observacoesGerais, onValueChange = { observacoesGerais = it }, label = { Text("Observações Gerais") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = fieldColors)
            }

            FormSection("Meio de Entrada") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    listOf("Aéreo", "Subterrâneo").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { meioEntrada = option }) {
                            RadioButton(selected = meioEntrada == option, onClick = { meioEntrada = option }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2D7DD2), unselectedColor = Color(0xFF6B7A99)))
                            Text(option, color = Color(0xFF1A2B4A), fontSize = 14.sp)
                        }
                    }
                }
            }

            FormSection("Tipo de Instalação") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    listOf("CTO", "Modular").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tipoInstalacao = option }) {
                            RadioButton(selected = tipoInstalacao == option, onClick = { tipoInstalacao = option }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2D7DD2), unselectedColor = Color(0xFF6B7A99)))
                            Text(option, color = Color(0xFF1A2B4A), fontSize = 14.sp)
                        }
                    }
                }
            }

            FormSection("Responsável") {
                OutlinedTextField(value = responsavelNome, onValueChange = { responsavelNome = it }, label = { Text("Nome do responsável") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors, singleLine = true)
                OutlinedTextField(
                    value = responsavelTelefone,
                    onValueChange = { newValue -> val numbers = newValue.filter { it.isDigit() }; if (numbers.length <= 11) responsavelTelefone = numbers },
                    label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    placeholder = { Text("00000000000", color = Color(0xFFAAB4C8)) },
                    colors = fieldColors, singleLine = true
                )
            }

            // ── Resultado da Vistoria ───────────────────────────────
            FormSection("Resultado da Vistoria") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(true to "Aprovado", false to "Reprovado").forEach { (valor, label) ->
                        val selected = aprovado == valor
                        val bgColor = when { selected && valor -> Color(0xFF1E7E45); selected && !valor -> Color(0xFFC0392B); else -> Color(0xFFF5F6FA) }
                        val textColor = if (selected) Color.White else Color(0xFF6B7A99)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { aprovado = if (aprovado == valor) null else valor }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textColor)
                        }
                    }
                }
                OutlinedTextField(
                    value = motivoResultado,
                    onValueChange = { motivoResultado = it },
                    label = { Text(if (aprovado == false) "Motivo da reprovação" else "Justificativa") },
                    placeholder = { Text("Descreva o motivo do resultado...", color = Color(0xFFAAB4C8)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = fieldColors
                )
            }

            Button(
                onClick = {
                    try {
                        Log.d("EditReportScreen", "Iniciando salvamento do relatório")
                        if (nomeEdificio.isEmpty() || endereco.isEmpty() || bairro.isEmpty()) {
                            Log.d("EditReportScreen", "Campos obrigatórios não preenchidos")
                            errorMessage = "Por favor, preencha o nome do edifício, endereço e bairro"
                            showError = true
                            return@Button
                        }
                        val enderecoCompleto = "$endereco, $bairro"
                        val updatedReport = report.copy(
                            nomeEdificio = nomeEdificio,
                            nome = nome,
                            endereco = enderecoCompleto,
                            andares = andares.toIntOrNull() ?: 0,
                            andaresSelecionados = andaresSelecionados.toList(),
                            apartamentosPorAndar = apartamentosPorAndar.toIntOrNull() ?: 0,
                            provedores = provedores.toIntOrNull() ?: 0,
                            nomesProvedores = nomesProvedores,
                            vagasOcupadasProvedores = vagasOcupadasProvedores,
                            andaresProvedores = andaresProvedores,
                            condicaoShaft = condicaoShaft,
                            fotosShaft = shaftPhotos,
                            meioEntrada = meioEntrada,
                            tipoInstalacao = tipoInstalacao,
                            observacoesGerais = observacoesGerais,
                            photos = photos,
                            responsavelNome = responsavelNome,
                            responsavelTelefone = responsavelTelefone,
                            responsavelVistoriaNome = responsavelVistoriaNome,
                            quantidadeBlocos = quantidadeBlocos,
                            cep = cep,
                            date = Date(),
                            aprovado = aprovado,
                            motivoResultado = motivoResultado
                        )
                        viewModel.updateReport(updatedReport)
                        Log.d("EditReportScreen", "Relatório atualizado com sucesso")
                        navController.popBackStack()
                        Toast.makeText(context, "Relatório atualizado com sucesso", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("EditReportScreen", "Erro ao atualizar relatório: ${e.message}", e)
                        errorMessage = "Erro ao atualizar relatório: ${e.message}"
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D7DD2)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Salvar Relatório", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    selectedPhotoUrl?.let { url ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { selectedPhotoUrl = null }, contentAlignment = Alignment.Center) {
            Image(painter = rememberAsyncImagePainter(url), contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
fun RequestPermission(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    val context = LocalContext.current
    val permissionCheck = ContextCompat.checkSelfPermission(
        context,
        permission
    )

    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
        onPermissionGranted()
    } else {
        launcher.launch(permission)
    }
}

@Composable
fun OpenSettings() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    launcher.launch(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = AndroidUri.fromParts("package", "com.example.relatorios", null)
        }
    )
}

private fun savePhotoToInternalStorage(context: AndroidContext, uri: AndroidUri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val fileName = "photo_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, fileName)
    
    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    
    return file.absolutePath
}

private fun loadPhotoFromInternalStorage(context: AndroidContext, path: String): AndroidUri {
    val file = File(path)
    return AndroidUri.fromFile(file)
}

private fun generatePdf(context: AndroidContext, report: Report) {
    try {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fileName = "relatorio_${report.nomeEdificio}_${dateFormat.format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(AndroidEnvironment.DIRECTORY_DOCUMENTS), fileName)
        
        // Verificar se o diretório existe, se não, criar
        file.parentFile?.mkdirs()
        
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        // ── Cabeçalho com logo ────────────────────────────────────────
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_myconnect)
            val logoTmp = File(context.cacheDir, "logo_pdf_tmp.png")
            JavaFileOutputStream(logoTmp).use { fos -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos) }

            val logoImg = ITextImage(ImageDataFactory.create(logoTmp.absolutePath)).scaleToFit(160f, 55f)

            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                .useAllAvailableWidth()
                .setMarginBottom(6f)

            headerTable.addCell(
                Cell().add(logoImg)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(ITextVerticalAlignment.MIDDLE)
            )
            headerTable.addCell(
                Cell()
                    .add(Paragraph("Relatório de Vistoria Técnica")
                        .setBold().setFontSize(13f)
                        .setTextAlignment(TextAlignment.RIGHT))
                    .add(Paragraph(dateFormat.format(report.date))
                        .setFontSize(10f)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(ColorConstants.GRAY))
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(ITextVerticalAlignment.MIDDLE)
            )

            document.add(headerTable)
            document.add(LineSeparator(SolidLine(1f)))
            document.add(Paragraph("\n").setFontSize(4f))
            logoTmp.delete()
        } catch (e: Exception) {
            Log.e("PDFGeneration", "Erro ao adicionar logo ao PDF: ${e.message}")
        }

        // ── Cores e helpers ───────────────────────────────────────────
        val navy      = DeviceRgb(26, 43, 74)
        val blue      = DeviceRgb(45, 125, 210)
        val rowAlt    = DeviceRgb(240, 242, 245)
        val rowBorder = DeviceRgb(220, 224, 234)
        val labelGray = DeviceRgb(107, 122, 153)

        fun secHeader(title: String): Table {
            val t = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
                .useAllAvailableWidth().setMarginTop(10f).setMarginBottom(0f)
            t.addCell(
                Cell().add(Paragraph(title).setBold().setFontSize(10f).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(navy).setPadding(5f).setBorder(Border.NO_BORDER)
            )
            return t
        }

        fun infoTable(vararg rows: Pair<String, String>): Table {
            val t = Table(UnitValue.createPercentArray(floatArrayOf(38f, 62f))).useAllAvailableWidth()
            rows.forEachIndexed { i, (label, value) ->
                val bg = if (i % 2 == 0) rowAlt else ColorConstants.WHITE
                val bot = SolidBorder(rowBorder, 0.5f)
                t.addCell(Cell().add(Paragraph(label).setBold().setFontSize(9f).setFontColor(labelGray))
                    .setBackgroundColor(bg).setPaddingLeft(8f).setPaddingTop(5f).setPaddingBottom(5f)
                    .setBorderLeft(Border.NO_BORDER).setBorderTop(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER).setBorderBottom(bot))
                t.addCell(Cell().add(Paragraph(value.ifEmpty { "-" }).setFontSize(9f))
                    .setBackgroundColor(bg).setPaddingLeft(8f).setPaddingTop(5f).setPaddingBottom(5f)
                    .setBorderLeft(Border.NO_BORDER).setBorderTop(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER).setBorderBottom(bot))
            }
            return t
        }

        // ── Identificação do Edifício ─────────────────────────────────
        val cepFmt = report.cep.takeIf { it.length == 8 }?.let { "${it.take(5)}-${it.drop(5)}" } ?: report.cep.ifEmpty { "-" }
        document.add(secHeader("IDENTIFICAÇÃO DO EDIFÍCIO"))
        document.add(infoTable(
            "Nome do Edifício"        to report.nomeEdificio,
            "CEP"                     to cepFmt,
            "Endereço"                to report.endereco,
            "Andares"                 to report.andares.toString(),
            "Aptos por Andar"         to report.apartamentosPorAndar.toString(),
            "Qtd. de Blocos"          to (report.quantidadeBlocos ?: "-"),
            "Andares p/ CTO/Modular"  to report.andaresSelecionados.joinToString(", ").ifEmpty { "-" }
        ))

        // ── Infraestrutura ────────────────────────────────────────────
        document.add(secHeader("INFRAESTRUTURA"))
        document.add(infoTable(
            "Meio de Entrada"   to report.meioEntrada,
            "Tipo de Instalação" to report.tipoInstalacao,
            "Condição do Shaft" to report.condicaoShaft.ifEmpty { "-" }
        ))

        // ── Provedores ────────────────────────────────────────────────
        document.add(secHeader("PROVEDORES (${report.provedores})"))
        if (report.nomesProvedores.isNotEmpty()) {
            val pt = Table(UnitValue.createPercentArray(floatArrayOf(8f, 38f, 27f, 27f))).useAllAvailableWidth()
            listOf("#", "Nome", "Vagas Ocupadas", "Andares das Caixas").forEach { h ->
                pt.addHeaderCell(Cell().add(Paragraph(h).setBold().setFontSize(9f).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(blue).setPadding(5f).setBorder(Border.NO_BORDER))
            }
            report.nomesProvedores.forEachIndexed { i, nome ->
                val bg  = if (i % 2 == 0) rowAlt else ColorConstants.WHITE
                val bot = SolidBorder(rowBorder, 0.5f)
                fun pc(text: String) = Cell().add(Paragraph(text.ifEmpty { "-" }).setFontSize(9f))
                    .setBackgroundColor(bg).setPadding(5f)
                    .setBorderLeft(Border.NO_BORDER).setBorderTop(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER).setBorderBottom(bot)
                pt.addCell(pc("${i + 1}"))
                pt.addCell(pc(nome))
                pt.addCell(pc(report.vagasOcupadasProvedores.getOrElse(i) { "" }))
                pt.addCell(pc(report.andaresProvedores.getOrElse(i) { "" }))
            }
            document.add(pt)
        } else {
            document.add(Paragraph("Nenhum provedor informado.")
                .setFontSize(9f).setFontColor(labelGray).setPaddingLeft(8f).setPaddingTop(5f))
        }

        // ── Observações Gerais ────────────────────────────────────────
        if (report.observacoesGerais.isNotEmpty()) {
            document.add(secHeader("OBSERVAÇÕES GERAIS"))
            document.add(Paragraph(report.observacoesGerais)
                .setFontSize(9f).setPaddingLeft(8f).setPaddingTop(6f).setPaddingBottom(6f))
        }

        // ── Resultado ─────────────────────────────────────────────────
        document.add(secHeader("RESULTADO DA VISTORIA"))
        document.add(infoTable(
            "Resultado" to when (report.aprovado) {
                true  -> "Aprovado"
                false -> "Reprovado"
                null  -> "Não avaliado"
            },
            "Justificativa" to report.motivoResultado.ifEmpty { "-" }
        ))

        // ── Responsáveis ──────────────────────────────────────────────
        document.add(secHeader("RESPONSÁVEIS"))
        document.add(infoTable(
            "Responsável pela Vistoria" to report.responsavelVistoriaNome.ifEmpty { "-" },
            "Responsável do Edifício"   to report.responsavelNome.ifEmpty { "-" },
            "Telefone"                  to report.responsavelTelefone.ifEmpty { "-" }
        ))

        // ── Fotos do Shaft ────────────────────────────────────────────
        if (report.fotosShaft.isNotEmpty()) {
            document.add(secHeader("FOTOS DO SHAFT"))
            val shaftGrid = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth().setMarginTop(4f)
            var shaftCols = 0
            for (photoPath in report.fotosShaft) {
                try {
                    val imageFile: File? = if (photoPath.startsWith("http"))
                        runBlocking { downloadImageToTempFile(context, photoPath, "shaft_photo_") }
                    else File(photoPath).takeIf { it.exists() }
                    if (imageFile != null && imageFile.exists()) {
                        val img = ITextImage(ImageDataFactory.create(imageFile.absolutePath)).setAutoScaleWidth(true)
                        shaftGrid.addCell(Cell().add(img).setBorder(Border.NO_BORDER).setPadding(4f))
                        if (photoPath.startsWith("http")) imageFile.delete()
                        shaftCols++
                    }
                } catch (e: Exception) { Log.e("PDFGeneration", "Erro foto shaft: ${e.message}") }
            }
            if (shaftCols % 2 != 0) shaftGrid.addCell(Cell().setBorder(Border.NO_BORDER))
            document.add(shaftGrid)
        }

        // ── Fotos Gerais ──────────────────────────────────────────────
        if (report.photos.isNotEmpty()) {
            document.add(secHeader("FOTOS GERAIS"))
            val photoGrid = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth().setMarginTop(4f)
            var photoCols = 0
            for (photoPath in report.photos) {
                try {
                    val imageFile: File? = if (photoPath.startsWith("http"))
                        runBlocking { downloadImageToTempFile(context, photoPath, "general_photo_") }
                    else File(photoPath).takeIf { it.exists() }
                    if (imageFile != null && imageFile.exists()) {
                        val img = ITextImage(ImageDataFactory.create(imageFile.absolutePath)).setAutoScaleWidth(true)
                        photoGrid.addCell(Cell().add(img).setBorder(Border.NO_BORDER).setPadding(4f))
                        if (photoPath.startsWith("http")) imageFile.delete()
                        photoCols++
                    }
                } catch (e: Exception) { Log.e("PDFGeneration", "Erro foto geral: ${e.message}") }
            }
            if (photoCols % 2 != 0) photoGrid.addCell(Cell().setBorder(Border.NO_BORDER))
            document.add(photoGrid)
        }

        document.close()
        
        // Abrir o PDF gerado
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        context.startActivity(intent)
        
    } catch (e: Exception) {
        Log.e("PDFGeneration", "Error generating PDF: ${e.message}")
        throw e
    }
}

private fun generateExcel(context: AndroidContext, reports: List<Report>) {
    var workbook: XSSFWorkbook? = null
    var outputStream: JavaFileOutputStream? = null
    
    try {
        Log.d("ExcelGeneration", "Iniciando geração do Excel")
        
        // Verificar se há relatórios para exportar
        if (reports.isEmpty()) {
            Log.d("ExcelGeneration", "Nenhum relatório para exportar")
            Toast.makeText(context, "Nenhum relatório para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar permissões de armazenamento
        if (AndroidBuild.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.R) {
            if (!AndroidEnvironment.isExternalStorageManager()) {
                Log.d("ExcelGeneration", "Solicitando permissão de gerenciamento de armazenamento")
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = AndroidUri.parse("package:${context.packageName}")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(context, "Por favor, conceda a permissão de armazenamento", Toast.LENGTH_LONG).show()
                return
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.d("ExcelGeneration", "Solicitando permissão de escrita")
                Toast.makeText(context, "Por favor, conceda a permissão de armazenamento", Toast.LENGTH_LONG).show()
                return
            }
        }

        Log.d("ExcelGeneration", "Criando workbook")
        workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Relatórios")
        
        Log.d("ExcelGeneration", "Configurando estilos")
        // Estilo para cabeçalho
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont()
            font.bold = true
            setFont(font)
        }
        
        // Estilo para células
        val cellStyle = workbook.createCellStyle().apply {
            wrapText = true
        }
        
        Log.d("ExcelGeneration", "Criando cabeçalho")
        // Criar cabeçalho
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Responsável Pela Vistoria")
        headerRow.createCell(1).setCellValue("Nome do Edifício")
        headerRow.createCell(2).setCellValue("Endereço")
        headerRow.createCell(3).setCellValue("Bairro")
        headerRow.createCell(4).setCellValue("Data")
        headerRow.createCell(5).setCellValue("Andares")
        headerRow.createCell(6).setCellValue("Andares para Instalação da CTO ou Modular")
        headerRow.createCell(7).setCellValue("Apartamentos por Andar")
        headerRow.createCell(8).setCellValue("Provedores")
        headerRow.createCell(9).setCellValue("Quantidade de Blocos")
        headerRow.createCell(10).setCellValue("Condições do Shaft")
        headerRow.createCell(11).setCellValue("Meio de Entrada")
        headerRow.createCell(12).setCellValue("Tipo de Instalação")
        headerRow.createCell(13).setCellValue("Observações Gerais")
        headerRow.createCell(14).setCellValue("Responsável")
        headerRow.createCell(15).setCellValue("Telefone do Responsável")
        headerRow.createCell(16).setCellValue("CEP")
        headerRow.createCell(17).setCellValue("Resultado")
        headerRow.createCell(18).setCellValue("Motivo/Justificativa")

        // Aplicar estilo de cabeçalho
        for (i in 0..18) {
            headerRow.getCell(i).cellStyle = headerStyle
        }
        
        Log.d("ExcelGeneration", "Preenchendo dados")
        // Preencher dados
        reports.forEachIndexed { rowIndex, report ->
            try {
                Log.d("ExcelGeneration", "Processando relatório ${rowIndex + 1}")
                val row = sheet.createRow(rowIndex + 1)
                
                // Extrair endereço e bairro
                val enderecoParts = report.endereco.split(",")
                val endereco = enderecoParts[0].trim()
                val bairro = if (enderecoParts.size > 1) enderecoParts[1].trim() else ""
                
                row.createCell(0).setCellValue(report.responsavelNome)
                row.createCell(1).setCellValue(report.nomeEdificio)
                row.createCell(2).setCellValue(endereco)
                row.createCell(3).setCellValue(bairro)
                row.createCell(4).setCellValue(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(report.date))
                row.createCell(5).setCellValue(report.andares.toDouble())
                row.createCell(6).setCellValue(report.andaresSelecionados.joinToString(", "))
                row.createCell(7).setCellValue(report.apartamentosPorAndar.toDouble())
                row.createCell(8).setCellValue(report.provedores.toDouble())
                row.createCell(9).setCellValue(report.quantidadeBlocos ?: "") // Trata como String, lida com nulo
                row.createCell(10).setCellValue(report.condicaoShaft)
                row.createCell(11).setCellValue(report.meioEntrada)
                row.createCell(12).setCellValue(report.tipoInstalacao)
                row.createCell(13).setCellValue(report.observacoesGerais)
                row.createCell(14).setCellValue(report.responsavelNome)
                row.createCell(15).setCellValue(report.responsavelTelefone)
                row.createCell(16).setCellValue(
                    report.cep.takeIf { it.isNotEmpty() }?.let { "${it.take(5)}-${it.drop(5)}" } ?: ""
                )
                row.createCell(17).setCellValue(when (report.aprovado) {
                    true  -> "Aprovado"
                    false -> "Reprovado"
                    null  -> ""
                })
                row.createCell(18).setCellValue(report.motivoResultado)

                // Aplicar estilo de quebra de texto em todas as células
                for (i in 0..18) {
                    row.getCell(i).cellStyle = cellStyle
                }
            } catch (e: Exception) {
                Log.e("ExcelGeneration", "Erro ao preencher dados da linha ${rowIndex + 1}: ${e.message}", e)
            }
        }
        
        Log.d("ExcelGeneration", "Definindo larguras fixas para as colunas")
        // Definir larguras fixas para as colunas
        sheet.setColumnWidth(0, 30 * 256)  // Responsável Pela Vistoria
        sheet.setColumnWidth(1, 30 * 256)  // Nome do Edifício
        sheet.setColumnWidth(2, 50 * 256)  // Endereço
        sheet.setColumnWidth(3, 20 * 256)  // Bairro
        sheet.setColumnWidth(4, 15 * 256)  // Data
        sheet.setColumnWidth(5, 15 * 256)  // Andares
        sheet.setColumnWidth(6, 20 * 256)  // Andares para Instalação da CTO ou Modular
        sheet.setColumnWidth(7, 20 * 256)  // Apartamentos por Andar
        sheet.setColumnWidth(8, 15 * 256)  // Provedores
        sheet.setColumnWidth(9, 20 * 256)  // Quantidade de Blocos (ajustado para texto)
        sheet.setColumnWidth(10, 50 * 256)  // Condições do Shaft
        sheet.setColumnWidth(11, 20 * 256)  // Meio de Entrada
        sheet.setColumnWidth(12, 20 * 256)  // Tipo de Instalação
        sheet.setColumnWidth(13, 50 * 256) // Observações Gerais
        sheet.setColumnWidth(14, 30 * 256) // Responsável (Edifício)
        sheet.setColumnWidth(15, 20 * 256) // Telefone do Responsável (Edifício)
        sheet.setColumnWidth(16, 15 * 256) // CEP
        sheet.setColumnWidth(17, 15 * 256) // Resultado
        sheet.setColumnWidth(18, 50 * 256) // Motivo/Justificativa

        Log.d("ExcelGeneration", "Preparando para salvar arquivo")
        // Salvar arquivo no diretório de downloads
        val fileName = "relatorios_${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}.xlsx"
        val downloadsDir = AndroidEnvironment.getExternalStoragePublicDirectory(AndroidEnvironment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        
        Log.d("ExcelGeneration", "Caminho do arquivo: ${file.absolutePath}")
        
        // Garantir que o diretório existe
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        Log.d("ExcelGeneration", "Salvando arquivo")
        // Salvar o arquivo
        try {
            outputStream = JavaFileOutputStream(file)
            workbook.write(outputStream)
            outputStream.flush()
            
            Log.d("ExcelGeneration", "Arquivo salvo com sucesso")
            
            // Criar o URI usando FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            Log.d("ExcelGeneration", "URI criada: $uri")
            
            // Criar o intent para abrir o arquivo
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Criar o intent de escolha
            val chooserIntent = Intent.createChooser(
                openIntent,
                "Escolha um aplicativo para abrir o arquivo"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            Log.d("ExcelGeneration", "Iniciando escolha do aplicativo")
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            Log.e("ExcelGeneration", "Erro ao salvar arquivo: ${e.message}", e)
            Toast.makeText(
                context,
                "Erro ao salvar arquivo: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
        
    } catch (e: Exception) {
        Log.e("ExcelGeneration", "Erro ao gerar Excel: ${e.message}", e)
        Toast.makeText(
            context,
            "Erro ao gerar Excel: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    } finally {
        try {
            outputStream?.close()
            workbook?.close()
        } catch (e: Exception) {
            Log.e("ExcelGeneration", "Erro ao fechar recursos: ${e.message}", e)
        }
    }
}



private fun downloadAndInstallUpdate(context: AndroidContext, downloadUrl: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(AndroidUri.parse(downloadUrl))
            .setTitle("Atualizando o aplicativo")
            .setDescription("Baixando nova versão")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(AndroidEnvironment.DIRECTORY_DOWNLOADS, "vistoriaapp.apk")

        val downloadId = downloadManager.enqueue(request)

        // Registrar um BroadcastReceiver para monitorar o download
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        
                        if (cursor.moveToFirst()) {
                            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    val apkFile = File(AndroidEnvironment.getExternalStoragePublicDirectory(AndroidEnvironment.DIRECTORY_DOWNLOADS), "vistoriaapp.apk")
                                    val apkUri = if (AndroidBuild.VERSION.SDK_INT >= 24) {
                                        FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
                                    } else {
                                        AndroidUri.fromFile(apkFile)
                                    }
                                    
                                    // Criar canal de notificação para Android 8.0 e superior
                                    if (AndroidBuild.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.O) {
                                        val channel = NotificationChannel(
                                            "update_channel",
                                            "Atualizações",
                                            NotificationManager.IMPORTANCE_HIGH
                                        )
                                        val notificationManager = context.getSystemService(NotificationManager::class.java)
                                        notificationManager.createNotificationChannel(channel)
                                    }
                                    
                                    // Criar intent para instalar o APK
                                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    
                                    // Criar PendingIntent para a notificação
                                    val pendingIntent = PendingIntent.getActivity(
                                        context,
                                        0,
                                        installIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )
                                    
                                    // Criar e mostrar a notificação
                                    val notification = NotificationCompat.Builder(context, "update_channel")
                                        .setContentTitle("Atualização disponível")
                                        .setContentText("Toque para instalar a nova versão")
                                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                        .setAutoCancel(true)
                                        .setContentIntent(pendingIntent)
                                        .build()
                                    
                                    NotificationManagerCompat.from(context).notify(1, notification)
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                                    val errorMessage = when (reason) {
                                        DownloadManager.ERROR_CANNOT_RESUME -> "Não foi possível retomar o download"
                                        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Dispositivo de armazenamento não encontrado"
                                        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "O arquivo já existe"
                                        DownloadManager.ERROR_FILE_ERROR -> "Erro ao salvar o arquivo"
                                        DownloadManager.ERROR_HTTP_DATA_ERROR -> "Erro nos dados HTTP"
                                        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Espaço insuficiente no dispositivo"
                                        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Muitos redirecionamentos"
                                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Código HTTP não tratado"
                                        else -> "Erro desconhecido no download"
                                    }
                                    
                                    Handler(Looper.getMainLooper()).post {
                                        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
                                        builder.setTitle("Erro no Download")
                                        builder.setMessage(errorMessage)
                                        builder.setPositiveButton("OK") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        builder.show()
                                    }
                                }
                            }
                        }
                        cursor.close()
                    }
                }
            }
        }

        val flags = if (AndroidBuild.VERSION.SDK_INT >= 33) {
            Context.RECEIVER_NOT_EXPORTED
        } else 0

        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            flags
        )
    } catch (e: Exception) {
        Log.e("UpdateCheck", "Erro ao iniciar download: ${e.message}", e)
        Handler(Looper.getMainLooper()).post {
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Erro no Download")
            builder.setMessage("Não foi possível iniciar o download da atualização: ${e.message}")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }
}

private fun sha1Hex(input: String): String {
    val bytes = java.security.MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// Função utilitária para baixar imagem de URL para arquivo temporário (thread segura)
suspend fun downloadImageToTempFile(context: Context, urlString: String, prefix: String): File? {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val tempFile = File.createTempFile(prefix, ".jpg", context.cacheDir)
            url.openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("PDFGeneration", "Imagem baixada para: ${tempFile.absolutePath}")
            tempFile
        }
    } catch (e: Exception) {
        Log.e("PDFGeneration", "Erro ao baixar imagem de $urlString: ${e.message}")
        null
    }
}

// Função para fazer upload de imagem para o Imgur
private fun uploadImageToImgur(
    imagePath: String,
    clientId: String,
    callback: (String?) -> Unit
) {
    val url = "https://api.imgur.com/3/image"
    val imageFile = File(imagePath)

    if (!imageFile.exists()) {
        Log.e("ImgurUpload", "Arquivo não encontrado: $imagePath")
        callback(null)
        return
    }

    val requestBody = okhttp3.MultipartBody.Builder()
        .setType(okhttp3.MultipartBody.FORM)
        .addFormDataPart("image", imageFile.name, okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), imageFile))
        .build()

    val request = okhttp3.Request.Builder()
        .url(url)
        .header("Authorization", "Client-ID $clientId") // Usar o Client ID aqui
        .post(requestBody)
        .build()

    okhttp3.OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            Log.e("ImgurUpload", "Falha no upload para o Imgur: ${e.message}", e)
            callback(null)
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            response.use {
                if (!it.isSuccessful) {
                    Log.e("ImgurUpload", "Upload para o Imgur falhou: ${it.code} - ${it.message}")
                    callback(null)
                    return
                }

                val responseBody = it.body?.string()
                if (responseBody == null) {
                    Log.e("ImgurUpload", "Resposta vazia do Imgur")
                    callback(null)
                    return
                }

                try {
                    val json = org.json.JSONObject(responseBody)
                    val data = json.optJSONObject("data")
                    val link = data?.optString("link")
                    
                    if (link != null) {
                        Log.d("ImgurUpload", "Upload para o Imgur bem-sucedido: $link")
                        callback(link)
                    } else {
                        Log.e("ImgurUpload", "Link da imagem não encontrado na resposta do Imgur: $responseBody")
                        callback(null)
                    }
                } catch (e: Exception) {
                    Log.e("ImgurUpload", "Erro ao processar resposta JSON do Imgur: ${e.message}", e)
                    callback(null)
                }
            }
        }
    })
}

// Função auxiliar para upload de foto (usará a função de upload para o Imgur)
private fun uploadPhoto(
    viewModel: ReportViewModel,
    file: File,
    isShaftPhoto: Boolean = false,
    onPhotoUploaded: ((String?) -> Unit)? = null
) {
    val clientId = "9e51efd7a8b3c91" // Seu Client ID do Imgur
    uploadImageToImgur(
        file.absolutePath,
        clientId
    ) { link ->
        if (link != null) {
            // Adicionar o link da imagem ao relatório no ViewModel
            if (isShaftPhoto) {
                Log.d("EditReportScreen", "Adicionando foto shaft: $link")
                viewModel.addShaftPhotoToReport(link)
                Log.d("addShaftPhotoToReport", "Adicionando foto shaft: $link")
                Log.d("ImgurUpload", "Link da imagem Imgur (Shaft) salvo no Report: $link")
            } else {
                viewModel.addPhotoToReport(link)
                Log.d("ImgurUpload", "Link da imagem Imgur (Geral) salvo no Report: $link")
            }
        } else {
            Log.e("ImgurUpload", "Falha no upload da imagem para o Imgur")
            // Opcional: Mostrar uma mensagem de erro para o usuário
        }
        // Callback para atualizar o estado local na tela
        onPhotoUploaded?.invoke(link)
    }
}