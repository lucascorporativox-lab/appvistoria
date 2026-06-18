package com.example.relatorios.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.relatorios.model.TipoInstalacao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CtoModularForm(
    ctoAndares: Int,
    quantidadeProvedores: Int,
    tipoInstalacao: TipoInstalacao,
    onCtoAndaresChange: (Int) -> Unit,
    onQuantidadeProvedoresChange: (Int) -> Unit,
    onTipoInstalacaoChange: (TipoInstalacao) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Input para quantidade de CTO e Andares
        OutlinedTextField(
            value = ctoAndares.toString(),
            onValueChange = { 
                val value = it.toIntOrNull() ?: 0
                onCtoAndaresChange(value)
            },
            label = { Text("Quantidade de CTO e Andares") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Input para quantidade de provedores
        OutlinedTextField(
            value = quantidadeProvedores.toString(),
            onValueChange = { 
                val value = it.toIntOrNull() ?: 0
                onQuantidadeProvedoresChange(value)
            },
            label = { Text("Quantidade de Provedores") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Dropdown para seleção de CTO ou Modular
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = tipoInstalacao.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Instalação") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                TipoInstalacao.values().forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.name) },
                        onClick = {
                            onTipoInstalacaoChange(tipo)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
} 