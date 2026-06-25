package com.example.relatorios.data.model

import java.util.*

data class Report(
    val id: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val endereco: String = "",
    val nomeEdificio: String = "",
    val andares: Int = 0,
    val andaresSelecionados: List<Int> = emptyList(),
    val apartamentosPorAndar: Int = 0,
    val provedores: Int = 0,
    val nomesProvedores: List<String> = emptyList(),
    val vagasOcupadasProvedores: List<String> = emptyList(),
    val andaresProvedores: List<String> = emptyList(),
    val condicaoShaft: String = "",
    val fotosShaft: List<String> = emptyList(),
    val meioEntrada: String = "",
    val tipoInstalacao: String = "",
    val observacoesGerais: String = "",
    val photos: List<String> = emptyList(),
    val responsavelNome: String = "", // Responsável pelo edifício
    val responsavelTelefone: String = "",
    val responsavelVistoriaNome: String = "", // Responsável pela vistoria
    val responsavelVistoriaTelefone: String = "",
    val quantidadeBlocos: String? = "0",
    val cep: String = "",
    val date: Date = Date(),
    val aprovado: Boolean? = null,
    val motivoResultado: String = ""
) 