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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Checkbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagem de fundo
        Image(
            painter = painterResource(id = R.drawable.fundo),
            contentDescription = "Imagem de fundo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1722F8) // Azul #1722f8
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Relatórios", color = Color.White) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Novo", color = Color.White) }
                )
            }
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        when (selectedTab) {
                            0 -> ReportListScreen(navController, viewModel, searchQuery)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    navController: androidx.navigation.NavController,
    viewModel: ReportViewModel,
    searchQuery: String
) {
    val reports by viewModel.reports.collectAsState()
    val context = LocalContext.current
    var selectedBairro by remember { mutableStateOf("") }
    var expandedBairro by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reportToDelete by remember { mutableStateOf<Report?>(null) }
    
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
            onDismissRequest = { 
                showDeleteDialog = false
                reportToDelete = null
            },
            title = { 
                Text(
                    "Confirmar Exclusão",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = { 
                Text(
                    "Tem certeza que deseja excluir o relatório de ${reportToDelete?.nomeEdificio}?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reportToDelete?.let { report ->
                            viewModel.deleteReport(report.id)
                            Toast.makeText(context, "Relatório excluído com sucesso", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteDialog = false
                        reportToDelete = null
                    }
                ) {
                    Text(
                        "Excluir",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        reportToDelete = null
                    }
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Barra de pesquisa com fundo semitransparente
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { /* A mudança é gerenciada pelo componente pai */ },
                label = { Text("Pesquisar") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )
            
            // Lista de relatórios
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (reports.isEmpty()) {
                    item {
                        Surface(
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Nenhum relatório encontrado",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items = reports,
                        key = { it.id }
                    ) { report ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = Color(0xFF1722F8),
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
                                    IconButton(onClick = { 
                                        reportToDelete = report
                                        showDeleteDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Delete, 
                                            contentDescription = "Excluir",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = { 
                                        navController.navigate("edit_report/${report.id}")
                                    }) {
                                        Icon(
                                            Icons.Default.Edit, 
                                            contentDescription = "Editar",
                                            tint = Color.White
                                        )
                                    }
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
                    }
                }
            }

            // Filtro e botão de exportar com fundo azul
            Surface(
                color = Color(0xFF1722F8),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filtro por bairro
                    ExposedDropdownMenuBox(
                        expanded = expandedBairro,
                        onExpandedChange = { expandedBairro = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedBairro,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por Bairro", color = Color.White) },
                            trailingIcon = { 
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBairro)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedBairro,
                            onDismissRequest = { expandedBairro = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos os Bairros") },
                                onClick = {
                                    selectedBairro = ""
                                    expandedBairro = false
                                }
                            )
                            bairrosList.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        selectedBairro = item
                                        expandedBairro = false
                                    }
                                )
                            }
                        }
                    }

                    // Botão de exportar Excel
                    IconButton(
                        onClick = {
                            try {
                                // Filtrar os relatórios pelo bairro selecionado antes de exportar
                                val filteredReports = if (selectedBairro.isNullOrBlank()) {
                                    reports
                                } else {
                                    reports.filter { report ->
                                        // Extrai bairro do endereço
                                        val enderecoParts = report.endereco.split(",")
                                        val bairro = if (enderecoParts.size > 1) enderecoParts[1].trim() else ""
                                        bairro.equals(selectedBairro, ignoreCase = true)
                                    }
                                }
                                generateExcel(context, filteredReports)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao gerar Excel: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
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
            title = { 
                Text(
                    "Confirmar Exclusão",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = { 
                Text(
                    "Tem certeza que deseja excluir o relatório de ${report.nomeEdificio}?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        "Excluir",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.bodyLarge
                    )
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
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
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
            title = { Text("Selecionar foto") },
            text = {
                Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 16.dp))
                    TextButton(onClick = {
                        val photoFile = File(context.cacheDir, "camera_photo_general_${System.currentTimeMillis()}.jpg")
                        cameraGeneralImageUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            photoFile
                        )
                        takeGeneralPictureLauncher.launch(cameraGeneralImageUri)
                        showGeneralPhotoDialog = false
                    }) { Text("Câmera") }
                    TextButton(onClick = {
                        pickGeneralPhotoLauncher.launch("image/*")
                        showGeneralPhotoDialog = false
                    }) { Text("Galeria") }
                    // Opcional: Adicionar um botão de Cancelar
                    TextButton(onClick = { showGeneralPhotoDialog = false }) { Text("Cancelar") }
                }
            },
            // Remove confirmButton e dismissButton slots que estavam sendo usados incorretamente
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Diálogo de escolha de origem da foto do shaft
     if (showShaftPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showShaftPhotoDialog = false },
            title = { Text("Selecionar foto do Shaft") },
            text = {
                 Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 16.dp))
                    TextButton(onClick = {
                         val photoFile = File(context.cacheDir, "camera_photo_shaft_${System.currentTimeMillis()}.jpg")
                         cameraShaftImageUri = androidx.core.content.FileProvider.getUriForFile(
                             context,
                             context.packageName + ".provider",
                             photoFile
                         )
                        takeShaftPictureLauncher.launch(cameraShaftImageUri)
                        showShaftPhotoDialog = false
                    }) { Text("Câmera") }
                    TextButton(onClick = {
                        pickShaftPhotoLauncher.launch("image/*")
                        showShaftPhotoDialog = false
                    }) { Text("Galeria") }
                     // Opcional: Adicionar um botão de Cancelar
                    TextButton(onClick = { showShaftPhotoDialog = false }) { Text("Cancelar") }
                }
            },
            // Remove confirmButton e dismissButton slots que estavam sendo usados incorretamente
            confirmButton = {},
            dismissButton = {}
        )
    }

    if (showError) {
        LaunchedEffect(showError) {
            showError = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        ) {
            Text(errorMessage)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Relatórios",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo Responsável Pela Vistoria
        Text(
            text = "Responsável Pela Vistoria",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )
        OutlinedTextField(
            value = responsavelVistoriaNome,
            onValueChange = { responsavelVistoriaNome = it },
            label = { Text("Digite o nome do responsável pela vistoria", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        // Seção de Informações do Local
        Text(
            text = "Endereço",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = nomeEdificio,
                onValueChange = { nomeEdificio = it },
                label = { Text("Nome do Edifício", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )

            OutlinedTextField(
                value = endereco,
                onValueChange = { endereco = it },
                label = { Text("Rua, Número", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )

            ExposedDropdownMenuBox(
                expanded = expandedBairro,
                onExpandedChange = { expandedBairro = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bairro,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bairro", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBairro) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedBairro,
                    onDismissRequest = { expandedBairro = false }
                ) {
                    bairrosList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                bairro = item
                                expandedBairro = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = nome,
                onValueChange = { },
                label = { Text("Nome do Relatório", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )
        }

        // Seção de Informações do Edifício
        Text(
            text = "Informações do Edifício",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dropdown para número de andares
            ExposedDropdownMenuBox(
                expanded = expandedAndares,
                onExpandedChange = { expandedAndares = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = andares,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Número de Andares", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndares) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedAndares,
                    onDismissRequest = { expandedAndares = false }
                ) {
                    andaresArray.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                andares = item
                                expandedAndares = false
                            }
                        )
                    }
                }
            }

            // Dropdown para apartamentos por andar
            ExposedDropdownMenuBox(
                expanded = expandedAptos,
                onExpandedChange = { expandedAptos = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = apartamentosPorAndar,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aptos por Andar", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAptos) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedAptos,
                    onDismissRequest = { expandedAptos = false }
                ) {
                    aptosArray.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                apartamentosPorAndar = item
                                expandedAptos = false
                            }
                        )
                    }
                }
            }
        }

        // Campo para quantidade de blocos
        OutlinedTextField(
            value = quantidadeBlocos,
            onValueChange = { newValue ->
                quantidadeBlocos = newValue
            },
            label = { Text("Quantidade de Blocos", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        // Seção de Infraestrutura
        Text(
            text = "Infraestrutura",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )

        // Dropdown para número de provedores
        ExposedDropdownMenuBox(
            expanded = expandedProvedores,
            onExpandedChange = { expandedProvedores = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = provedores,
                onValueChange = {},
                readOnly = true,
                label = { Text("Número de Provedores", color = Color.White) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvedores) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expandedProvedores,
                onDismissRequest = { expandedProvedores = false }
            ) {
                provedoresArray.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            provedores = item
                            // Inicializa a lista de nomes de provedores com strings vazias
                            nomesProvedores = List(item.toInt()) { "" }
                            expandedProvedores = false
                        }
                    )
                }
            }
        }

        // Campos para nomes dos provedores
        if (provedores.isNotEmpty() && provedores.toIntOrNull() ?: 0 > 0) {
            Text(
                text = "Nomes dos Provedores",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.White
            )
            
            Column {
                repeat(provedores.toInt()) { index ->
                    OutlinedTextField(
                        value = nomesProvedores.getOrNull(index) ?: "",
                        onValueChange = { newValue ->
                            val newNomes = nomesProvedores.toMutableList()
                            if (index < newNomes.size) {
                                newNomes[index] = newValue
                            } else {
                                newNomes.add(newValue)
                            }
                            nomesProvedores = newNomes
                        },
                        label = { Text("Nome do Provedor ${index + 1}", color = Color.White) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                            focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            focusedBorderColor = Color.White
                        )
                    )
                }
            }
        }

        OutlinedTextField(
            value = condicaoShaft,
            onValueChange = { condicaoShaft = it },
            label = { Text("Condições do Shaft", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = observacoesGerais,
            onValueChange = { observacoesGerais = it },
            label = { Text("Observações Gerais", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão Adicionar Fotos do Shaft - Mantido aqui ou mover se necessário
        Button(
            onClick = { showShaftPhotoDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Adicionar Fotos do Shaft")
        }
        if (shaftPhotos.isNotEmpty()) {
            Text(
                text = "Fotos do Shaft:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = 8.dp)
            ) {
                items(shaftPhotos) { photoUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = "Foto do shaft",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 8.dp)
                            .clickable { selectedPhotoUrl = photoUrl },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Campo para meio de entrada como RadioGroup
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Meio de Entrada do Condomínio",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = meioEntrada == "Aéreo",
                        onClick = { meioEntrada = "Aéreo" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "Aéreo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = meioEntrada == "Subterrâneo",
                        onClick = { meioEntrada = "Subterrâneo" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "Subterrâneo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }
        }

        // Campo para tipo de instalação como RadioGroup
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Tipo de Instalação",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tipoInstalacao == "CTO",
                        onClick = { tipoInstalacao = "CTO" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "CTO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tipoInstalacao == "Modular",
                        onClick = { tipoInstalacao = "Modular" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "Modular",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }
        }

        // Seção de Possíveis Andares de Instalação
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Andares para Instalação da CTO ou Modular",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.White
            )
            
            ExposedDropdownMenuBox(
                expanded = expandedAndaresSelecionados,
                onExpandedChange = { expandedAndaresSelecionados = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (andaresSelecionados.isEmpty()) "Selecione os andares" 
                           else andaresSelecionados.sorted().joinToString(", "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Andares para Instalação da CTO ou Modular", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndaresSelecionados) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedAndaresSelecionados,
                    onDismissRequest = { expandedAndaresSelecionados = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            val totalAndaresInt = andares.toIntOrNull() ?: 0
                            if (totalAndaresInt > 0) {
                                for (andar in 1..totalAndaresInt) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                andaresSelecionados = if (andaresSelecionados.contains(andar)) {
                                                    andaresSelecionados.minus(andar)
                                                } else {
                                                    andaresSelecionados.plus(andar)
                                                }
                                            }
                                            .padding(vertical = 8.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = andaresSelecionados.contains(andar),
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Andar $andar",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Selecione o número de andares primeiro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Campos do responsável
        Text(
            text = "Responsável",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )
        
        OutlinedTextField(
            value = responsavelNome,
            onValueChange = { responsavelNome = it },
            label = { Text("Nome do Responsável", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        OutlinedTextField(
            value = responsavelTelefone,
            onValueChange = { newValue ->
                val numbers = newValue.filter { it.isDigit() }
                if (numbers.length <= 11) {
                    responsavelTelefone = numbers
                }
            },
            label = { Text("Telefone do Responsável", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            placeholder = { Text("00000000000", color = Color.White.copy(alpha = 0.7f)) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )
        
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
                        date = Date()
                    )
                    
                    viewModel.addReport(newReport)
                    viewModel.setCurrentReport(newReport.id)
                    createdReportId = newReport.id
                    Toast.makeText(context, "Relatório salvo com sucesso", Toast.LENGTH_SHORT).show()
                    // Voltar para a aba de relatórios
                    onTabChange(0) // Muda para a aba de relatórios
                    navController.popBackStack()
                } catch (e: Exception) {
                    android.util.Log.e("NewReportScreen", "Error creating report: ${e.message}")
                    errorMessage = "Erro ao criar relatório: ${e.message}"
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Salvar Relatório")
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
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
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
            title = { Text("Selecionar foto") },
            text = {
                Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 16.dp))
                    TextButton(onClick = {
                        val photoFile = File(context.cacheDir, "camera_photo_general_${System.currentTimeMillis()}.jpg")
                        cameraGeneralImageUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            photoFile
                        )
                        takeGeneralPictureLauncher.launch(cameraGeneralImageUri)
                        showGeneralPhotoDialog = false
                    }) { Text("Câmera") }
                    TextButton(onClick = {
                        pickGeneralPhotoLauncher.launch("image/*")
                        showGeneralPhotoDialog = false
                    }) { Text("Galeria") }
                    // Opcional: Adicionar um botão de Cancelar
                    TextButton(onClick = { showGeneralPhotoDialog = false }) { Text("Cancelar") }
                }
            },
            // Remove confirmButton e dismissButton slots que estavam sendo usados incorretamente
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Diálogo de escolha de origem da foto do shaft
     if (showShaftPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showShaftPhotoDialog = false },
            title = { Text("Selecionar foto do Shaft") },
            text = {
                 Column {
                    Text("Escolha a origem da foto:", modifier = Modifier.padding(bottom = 16.dp))
                    TextButton(onClick = {
                         val photoFile = File(context.cacheDir, "camera_photo_shaft_${System.currentTimeMillis()}.jpg")
                         cameraShaftImageUri = androidx.core.content.FileProvider.getUriForFile(
                             context,
                             context.packageName + ".provider",
                             photoFile
                         )
                        takeShaftPictureLauncher.launch(cameraShaftImageUri)
                        showShaftPhotoDialog = false
                    }) { Text("Câmera") }
                    TextButton(onClick = {
                        pickShaftPhotoLauncher.launch("image/*")
                        showShaftPhotoDialog = false
                    }) { Text("Galeria") }
                     // Opcional: Adicionar um botão de Cancelar
                    TextButton(onClick = { showShaftPhotoDialog = false }) { Text("Cancelar") }
                }
            },
            // Remove confirmButton e dismissButton slots que estavam sendo usados incorretamente
            confirmButton = {},
            dismissButton = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Adicionar botão de voltar no topo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Voltar")
            }
        }
        


        // Campo Responsável Pela Vistoria
        Text(
            text = "Responsável Pela Vistoria",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )
        OutlinedTextField(
            value = responsavelVistoriaNome,
            onValueChange = { responsavelVistoriaNome = it },
            label = { Text("Digite o nome do responsável pela vistoria", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        // Seção de Informações do Local
        Text(
            text = "Endereço",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = nomeEdificio,
                onValueChange = { nomeEdificio = it },
                label = { Text("Nome do Edifício", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )

            OutlinedTextField(
                value = endereco,
                onValueChange = { endereco = it },
                label = { Text("Rua, Número", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )

            ExposedDropdownMenuBox(
                expanded = expandedBairro,
                onExpandedChange = { expandedBairro = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bairro,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bairro", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBairro) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedBairro,
                    onDismissRequest = { expandedBairro = false }
                ) {
                    bairrosList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                bairro = item
                                expandedBairro = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = nome,
                onValueChange = { },
                label = { Text("Nome do Relatório", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )
        }

        // Seção de Informações do Edifício
        Text(
            text = "Informações do Edifício",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dropdown para número de andares
            ExposedDropdownMenuBox(
                expanded = expandedAndares,
                onExpandedChange = { expandedAndares = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = andares,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Número de Andares", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndares) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedAndares,
                    onDismissRequest = { expandedAndares = false }
                ) {
                    andaresArray.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                andares = item
                                expandedAndares = false
                            }
                        )
                    }
                }
            }

            // Dropdown para apartamentos por andar
            ExposedDropdownMenuBox(
                expanded = expandedAptos,
                onExpandedChange = { expandedAptos = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = apartamentosPorAndar,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aptos por Andar", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAptos) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedAptos,
                    onDismissRequest = { expandedAptos = false }
                ) {
                    aptosArray.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                apartamentosPorAndar = item
                                expandedAptos = false
                            }
                        )
                    }
                }
            }
        }

        // Campo para quantidade de blocos
        OutlinedTextField(
            value = quantidadeBlocos,
            onValueChange = { newValue ->
                quantidadeBlocos = newValue
            },
            label = { Text("Quantidade de Blocos", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        // Seção de Infraestrutura
        Text(
            text = "Infraestrutura",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )

        // Dropdown para número de provedores
        ExposedDropdownMenuBox(
            expanded = expandedProvedores,
            onExpandedChange = { expandedProvedores = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = provedores,
                onValueChange = {},
                readOnly = true,
                label = { Text("Número de Provedores", color = Color.White) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvedores) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expandedProvedores,
                onDismissRequest = { expandedProvedores = false }
            ) {
                provedoresArray.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            provedores = item
                            // Inicializa a lista de nomes de provedores com strings vazias
                            nomesProvedores = List(item.toInt()) { "" }
                            expandedProvedores = false
                        }
                    )
                }
            }
        }

        // Campos para nomes dos provedores
        if (provedores.isNotEmpty() && provedores.toIntOrNull() ?: 0 > 0) {
            Text(
                text = "Nomes dos Provedores",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.White
            )
            
            Column {
                repeat(provedores.toInt()) { index ->
                    OutlinedTextField(
                        value = nomesProvedores.getOrNull(index) ?: "",
                        onValueChange = { newValue ->
                            val newNomes = nomesProvedores.toMutableList()
                            if (index < newNomes.size) {
                                newNomes[index] = newValue
                            } else {
                                newNomes.add(newValue)
                            }
                            nomesProvedores = newNomes
                        },
                        label = { Text("Nome do Provedor ${index + 1}", color = Color.White) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                            focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            focusedBorderColor = Color.White
                        )
                    )
                }
            }
        }

        OutlinedTextField(
            value = condicaoShaft,
            onValueChange = { condicaoShaft = it },
            label = { Text("Condições do Shaft", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = observacoesGerais,
            onValueChange = { observacoesGerais = it },
            label = { Text("Observações Gerais", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão Adicionar Fotos do Shaft - Mantido aqui ou mover se necessário
        Button(
            onClick = { showShaftPhotoDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Adicionar Fotos do Shaft")
        }
        // Exibição das fotos do shaft (logo após o botão)
        if (shaftPhotos.isNotEmpty()) {
            Text(
                text = "Fotos do Shaft Registradas:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = 8.dp)
            ) {
                items(shaftPhotos) { photoUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = "Foto do shaft registrada",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 8.dp)
                            .clickable { selectedPhotoUrl = photoUrl },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Campo para meio de entrada como RadioGroup
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Meio de Entrada do Condomínio",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = meioEntrada == "Aéreo",
                        onClick = { meioEntrada = "Aéreo" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "Aéreo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = meioEntrada == "Subterrâneo",
                        onClick = { meioEntrada = "Subterrâneo" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "Subterrâneo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }
        }

        // Campo para tipo de instalação como RadioGroup
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Tipo de Instalação",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tipoInstalacao == "CTO",
                        onClick = { tipoInstalacao = "CTO" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "CTO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tipoInstalacao == "Modular",
                        onClick = { tipoInstalacao = "Modular" },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Text(
                        text = "Modular",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }
        }

        // Seção de Possíveis Andares de Instalação
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Andares para Instalação da CTO ou Modular",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.White
            )
            
            ExposedDropdownMenuBox(
                expanded = expandedAndaresSelecionados,
                onExpandedChange = { expandedAndaresSelecionados = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (andaresSelecionados.isEmpty()) "Selecione os andares" 
                           else andaresSelecionados.sorted().joinToString(", "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Andares para Instalação da CTO ou Modular", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAndaresSelecionados) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedAndaresSelecionados,
                    onDismissRequest = { expandedAndaresSelecionados = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            val totalAndaresInt = andares.toIntOrNull() ?: 0
                            if (totalAndaresInt > 0) {
                                for (andar in 1..totalAndaresInt) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                andaresSelecionados = if (andaresSelecionados.contains(andar)) {
                                                    andaresSelecionados.minus(andar)
                                                } else {
                                                    andaresSelecionados.plus(andar)
                                                }
                                            }
                                            .padding(vertical = 8.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = andaresSelecionados.contains(andar),
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Andar $andar",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Selecione o número de andares primeiro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Campos do responsável
        Text(
            text = "Responsável",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White
        )
        
        OutlinedTextField(
            value = responsavelNome,
            onValueChange = { responsavelNome = it },
            label = { Text("Nome do Responsável", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )

        OutlinedTextField(
            value = responsavelTelefone,
            onValueChange = { newValue ->
                val numbers = newValue.filter { it.isDigit() }
                if (numbers.length <= 11) {
                    responsavelTelefone = numbers
                }
            },
            label = { Text("Telefone do Responsável", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            placeholder = { Text("00000000000", color = Color.White.copy(alpha = 0.7f)) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White
            )
        )
        
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
            Log.d("NewReportScreen", "Endereço completo: $enderecoCompleto")
            
            Log.d("NewReportScreen", "Criando novo relatório")
            val updatedReport = report.copy(
                nomeEdificio = nomeEdificio,
                nome = nome,
                endereco = enderecoCompleto,
                andares = andares.toIntOrNull() ?: 0,
                andaresSelecionados = andaresSelecionados.toList(),
                apartamentosPorAndar = apartamentosPorAndar.toIntOrNull() ?: 0,
                provedores = provedores.toIntOrNull() ?: 0,
                nomesProvedores = nomesProvedores,
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
                date = Date()
            )
            
            Log.d("EditReportScreen", "Atualizando relatório no ViewModel")
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
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Salvar Relatório")
        }
    }


    // Diálogo para exibir a foto em tamanho maior
    if (selectedPhotoUrl != null) {
        AlertDialog(
            onDismissRequest = { selectedPhotoUrl = null },
            title = { Text("Visualizar Foto") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedPhotoUrl),
                        contentDescription = "Foto expandida",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp), // Altura ajustável conforme necessário
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPhotoUrl = null }) {
                    Text("Fechar")
                }
            }
        )
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
        
        // Título
        document.add(Paragraph("Responsável Pela Vistoria: ${report.responsavelNome}").setBold().setFontSize(16f))
        document.add(Paragraph("Relatório do Edifício: ${report.nomeEdificio}").setBold().setFontSize(18f))
        document.add(Paragraph("Data: ${dateFormat.format(report.date)}"))

        // Informações do edifício em formato textual, não tabela
        document.add(Paragraph("Informações do Edifício").setFontSize(16f).setBold())
        document.add(Paragraph("Nome do Edifício: ${report.nomeEdificio}"))
        document.add(Paragraph("Endereço: ${report.endereco}"))
        document.add(Paragraph("Andares: ${report.andares}"))
        document.add(Paragraph("Andares para Instalação da CTO ou Modular: ${report.andaresSelecionados.joinToString(", ")}"))
        document.add(Paragraph("Apartamentos por Andar: ${report.apartamentosPorAndar}"))
        document.add(Paragraph("Quantidade de Blocos: ${report.quantidadeBlocos}"))
        document.add(Paragraph("Condição Shaft: ${report.condicaoShaft}"))
        document.add(Paragraph("Meio de Entrada: ${report.meioEntrada}"))
        document.add(Paragraph("Tipo de Instalação: ${report.tipoInstalacao}"))
        document.add(Paragraph("\n"))

        // Provedores
        if (report.nomesProvedores.isNotEmpty()) {
            document.add(Paragraph("Provedores Cadastrados").setFontSize(16f).setBold())
            for (nomeProvedor in report.nomesProvedores) {
                document.add(Paragraph("- $nomeProvedor").setFontSize(13f))
            }
            document.add(Paragraph("\n"))
        } else {
            document.add(Paragraph("Provedores Cadastrados: Nenhum informado").setFontSize(13f))
            document.add(Paragraph("\n"))
        }

        
        // Meio de Entrada e Tipo de Instalação
        document.add(Paragraph("Detalhes da Instalação").setFontSize(16f).setBold())
        document.add(Paragraph("Meio de Entrada: ${report.meioEntrada}"))
        document.add(Paragraph("Tipo de Instalação: ${report.tipoInstalacao}"))
        document.add(Paragraph("\n"))
        
        // Observações Gerais
        if (report.observacoesGerais.isNotEmpty()) {
            document.add(Paragraph("Observações Gerais").setFontSize(16f).setBold())
            document.add(Paragraph(report.observacoesGerais))
            document.add(Paragraph("\n"))
        }
        
        // Responsável
        document.add(Paragraph("Responsável").setFontSize(16f).setBold())
        document.add(Paragraph("Nome: ${report.responsavelNome}"))
        document.add(Paragraph("Telefone: ${report.responsavelTelefone}"))
        document.add(Paragraph("\n"))
        
        // Fotos do Shaft
        if (report.fotosShaft.isNotEmpty()) {
            document.add(Paragraph("Fotos do Shaft").setFontSize(16f).setBold())
            for (photoPath in report.fotosShaft) {
                Log.d("PDFGeneration", "Processando foto do shaft: $photoPath")
                try {
                    val imageFile: File? = if (photoPath.startsWith("http")) {
                        runBlocking {
                            Log.d("PDFGeneration", "Baixando imagem do shaft de URL: $photoPath (thread segura)")
                            downloadImageToTempFile(context, photoPath, "shaft_photo_")
                        }
                    } else {
                        val file = File(photoPath)
                        if (file.exists()) file else null
                    }
                    if (imageFile != null && imageFile.exists()) {
                        Log.d("PDFGeneration", "Arquivo de imagem existe: ${imageFile.absolutePath}")
                        val imageData = ImageDataFactory.create(imageFile.absolutePath)
                        val image = ITextImage(imageData)
                        image.setWidth(300f)
                        document.add(image)
                        Log.d("PDFGeneration", "Imagem do shaft adicionada ao PDF")
                        if (photoPath.startsWith("http")) {
                            imageFile.delete()
                            Log.d("PDFGeneration", "Arquivo temporário deletado: ${imageFile.absolutePath}")
                        }
                    } else {
                        Log.e("PDFGeneration", "Arquivo de imagem não existe: $photoPath")
                    }
                } catch (e: Exception) {
                    Log.e("PDFGeneration", "Error adding shaft photo: ${e.message}")
                }
            }
            document.add(Paragraph("\n"))
        }
        
        // Fotos Gerais
        if (report.photos.isNotEmpty()) {
            document.add(Paragraph("Fotos Gerais").setFontSize(16f).setBold())
            for (photoPath in report.photos) {
                Log.d("PDFGeneration", "Processando foto geral: $photoPath")
                try {
                    val imageFile: File? = if (photoPath.startsWith("http")) {
                        runBlocking {
                            Log.d("PDFGeneration", "Baixando imagem geral de URL: $photoPath (thread segura)")
                            downloadImageToTempFile(context, photoPath, "general_photo_")
                        }
                    } else {
                        val file = File(photoPath)
                        if (file.exists()) file else null
                    }
                    if (imageFile != null && imageFile.exists()) {
                        Log.d("PDFGeneration", "Arquivo de imagem existe: ${imageFile.absolutePath}")
                        val imageData = ImageDataFactory.create(imageFile.absolutePath)
                        val image = ITextImage(imageData)
                        image.setWidth(300f)
                        document.add(image)
                        Log.d("PDFGeneration", "Imagem geral adicionada ao PDF")
                        if (photoPath.startsWith("http")) {
                            imageFile.delete()
                            Log.d("PDFGeneration", "Arquivo temporário deletado: ${imageFile.absolutePath}")
                        }
                    } else {
                        Log.e("PDFGeneration", "Arquivo de imagem não existe: $photoPath")
                    }
                } catch (e: Exception) {
                    Log.e("PDFGeneration", "Error adding general photo: ${e.message}")
                }
            }
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
        
        // Aplicar estilo de cabeçalho
        for (i in 0..15) {
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
                
                // Aplicar estilo de quebra de texto em todas as células
                for (i in 0..15) {
                    row.getCell(i).cellStyle = cellStyle
                }
                if (row.lastCellNum > 16) {
                    for (i in 0..row.lastCellNum-1) {
                        row.getCell(i).cellStyle = cellStyle
                    }
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
        sheet.setColumnWidth(16, 30 * 256) // Responsável pela Vistoria (Novo campo)
        sheet.setColumnWidth(17, 20 * 256) // Telefone do Responsável pela Vistoria (Novo campo)
        
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