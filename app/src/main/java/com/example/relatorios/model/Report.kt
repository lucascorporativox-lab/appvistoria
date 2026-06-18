package com.example.relatorios.model

import java.util.Date

data class Report(
    val id: String,
    val nome: String,
    val endereco: String,
    val date: Date = Date(),
    val photos: List<String> = emptyList(),
    val nomeEdificio: String = "",
    val andares: Int = 0,
    val apartamentosPorAndar: Int = 0,
    val provedores: Int = 0,
    val condicaoShaft: String = "",
    val fotosShaft: List<String> = emptyList(),
    val meioEntrada: String = "",
    val tipoInstalacao: String = ""
)

enum class ReportStatus {
    DRAFT,
    COMPLETED,
    SUBMITTED
}

enum class MeioEntrada {
    INDEFINIDO,
    SUBTERRANEO,
    AEREO,
    MISTO
} 