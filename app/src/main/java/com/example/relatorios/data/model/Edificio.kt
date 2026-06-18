package com.example.relatorios.data.model

import java.util.UUID

data class Edificio(
    val id: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val endereco: String = "",
    val andares: Int = 0,
    val apartamentosPorAndar: Int = 0,
    val provedores: Int = 0,
    val nomesProvedores: List<String> = emptyList(),
    val condicaoShaft: String = "",
    val fotosShaft: List<String> = emptyList(),
    val meioEntrada: String = "",
    val tipoInstalacao: String = "",
    val photos: List<String> = emptyList(),
    val quantidadeBlocos: Int = 0
)