package com.example.relatorios.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.relatorios.R
import com.example.relatorios.data.model.Edificio
import com.example.relatorios.ui.components.EdificioForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdificioScreen(
    edificio: Edificio,
    onEdificioUpdated: (Edificio) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem de fundo
        Image(
            painter = painterResource(id = R.drawable.fundo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Conteúdo da tela
        EdificioForm(
            edificio = edificio,
            onEdificioUpdated = onEdificioUpdated,
            modifier = Modifier.fillMaxSize()
        )
    }
}
