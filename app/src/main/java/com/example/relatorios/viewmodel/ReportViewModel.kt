package com.example.relatorios.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.relatorios.data.model.Report
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val reportsCollection = db.collection("reports")
    private var reportsListener: ListenerRegistration? = null
    
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private var currentReportId: String? = null
    private var currentReport: Report? = null

    init {
        loadReports()
    }

    fun loadReports() {
        // Remove listener antigo, se houver
        reportsListener?.remove()
        reportsListener = reportsCollection
            // .orderBy("dataCriacao", Query.Direction.DESCENDING) // REMOVIDO PARA DEBUG
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ReportViewModel", "Erro ao escutar relatórios", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    Log.d("ReportViewModel", "Documentos brutos retornados: ${snapshot.documents.map { it.data }}")
                    val reportsList = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Tenta desserializar diretamente primeiro
                            doc.toObject(Report::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            // Se a desserialização direta falhar (ex: erro de tipo em quantidadeBlocos)
                            Log.e("ReportViewModel", "Erro desserializando documento ${doc.id}: ${e.message}", e)
                            // Mapeia manualmente, tratando quantidadeBlocos
                            val data = doc.data
                            if (data != null) {
                                val quantidadeBlocosValue = data["quantidadeBlocos"]
                                val quantidadeBlocosString = when (quantidadeBlocosValue) {
                                    is Long -> quantidadeBlocosValue.toString()
                                    is Double -> quantidadeBlocosValue.toString()
                                    is String -> quantidadeBlocosValue
                                    else -> "" // Valor padrão caso seja nulo ou outro tipo
                                }
                                Report(
                                    id = doc.id,
                                    nome = data["nome"] as? String ?: "",
                                    endereco = data["endereco"] as? String ?: "",
                                    nomeEdificio = data["nomeEdificio"] as? String ?: "",
                                    andares = (data["andares"] as? Long)?.toInt() ?: 0,
                                    andaresSelecionados = (data["andaresSelecionados"] as? List<*>)?.filterIsInstance<Long>()?.map { it.toInt() } ?: emptyList(),
                                    apartamentosPorAndar = (data["apartamentosPorAndar"] as? Long)?.toInt() ?: 0,
                                    provedores = (data["provedores"] as? Long)?.toInt() ?: 0,
                                    nomesProvedores = data["nomesProvedores"] as? List<String> ?: emptyList(),
                                    condicaoShaft = data["condicaoShaft"] as? String ?: "",
                                    fotosShaft = data["fotosShaft"] as? List<String> ?: emptyList(),
                                    meioEntrada = data["meioEntrada"] as? String ?: "",
                                    tipoInstalacao = data["tipoInstalacao"] as? String ?: "",
                                    observacoesGerais = data["observacoesGerais"] as? String ?: "",
                                    photos = data["photos"] as? List<String> ?: emptyList(),
                                    responsavelNome = data["responsavelNome"] as? String ?: "",
                                    responsavelTelefone = data["responsavelTelefone"] as? String ?: "",
                                    responsavelVistoriaNome = data["responsavelVistoriaNome"] as? String ?: "",
                                    quantidadeBlocos = quantidadeBlocosString,
                                    date = data["date"] as? Date ?: Date()
                                )
                            } else {
                                null // Não foi possível obter dados, ignora este documento
                            }
                        }
                    }.distinctBy { it.id } // Garante que não há IDs duplicados
                    _reports.value = reportsList
                    Log.d("ReportViewModel", "Relatórios carregados (tempo real): ${reportsList.size}")
                }
            }
    }

    fun addReport(report: Report) {
        Log.d("ReportViewModel", "Iniciando envio do relatório: $report")
        viewModelScope.launch {
            try {
                Log.d("ReportViewModel", "Enviando para o Firestore...")
                val docRef = reportsCollection.add(report).await()
                val newReport = report.copy(id = docRef.id)
                Log.d("ReportViewModel", "Relatório enviado com sucesso! ID gerado: ${docRef.id}")
                _reports.update { currentReports -> 
                    // Remove qualquer relatório com o mesmo ID antes de adicionar o novo
                    val filteredReports = currentReports.filter { it.id != newReport.id }
                    listOf(newReport) + filteredReports
                }
                Log.d("ReportViewModel", "Relatório adicionado à lista local com sucesso")
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Erro ao adicionar relatório: ${e.message}", e)
            }
        }
    }

    fun updateReport(report: Report) {
        viewModelScope.launch {
            try {
                report.id?.let { id ->
                    reportsCollection.document(id).set(report).await()
                    _reports.update { currentReports ->
                        currentReports.map { if (it.id == id) report else it }
                    }
                    if (currentReportId == id) {
                        currentReport = report
                    }
                    Log.d("ReportViewModel", "Relatório atualizado com sucesso")
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Erro ao atualizar relatório", e)
            }
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            try {
                reportsCollection.document(reportId).delete().await()
                _reports.update { currentReports ->
                    currentReports.filter { it.id != reportId }
                }
                if (currentReportId == reportId) {
                    currentReportId = null
                    currentReport = null
                }
                Log.d("ReportViewModel", "Relatório deletado com sucesso")
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Erro ao deletar relatório", e)
            }
        }
    }

    fun setCurrentReport(reportId: String) {
        currentReportId = reportId
        currentReport = _reports.value.find { it.id == reportId }
        Log.d("ReportViewModel", "Relatório atual definido: $reportId")
    }

    fun addPhotoToReport(photoUri: String) {
        currentReportId?.let { reportId ->
            try {
                val report = _reports.value.find { it.id == reportId }
                report?.let {
                    val updatedReport = it.copy(photos = it.photos + photoUri)
                    updateReport(updatedReport)
                    Log.d("ReportViewModel", "Foto adicionada com sucesso")
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Erro ao adicionar foto ao relatório", e)
            }
        }
    }

    // Nova função para adicionar foto à lista de fotos do shaft
    fun addShaftPhotoToReport(photoUri: String) {
    Log.d("addShaftPhotoToReport", "photoUri recebido: $photoUri")
        currentReportId?.let { reportId ->
            try {
                val report = _reports.value.find { it.id == reportId }
                report?.let {
                    Log.d("addShaftPhotoToReport", "Antes: ${it.fotosShaft}")
                val updatedReport = it.copy(fotosShaft = it.fotosShaft + photoUri)
                Log.d("addShaftPhotoToReport", "Depois: ${updatedReport.fotosShaft}")
                updateReport(updatedReport)
                Log.d("ReportViewModel", "Foto do Shaft adicionada com sucesso")
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Erro ao adicionar foto do Shaft ao relatório", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reportsListener?.remove()
    }
} 