package com.example.relatorios.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.relatorios.data.model.Edificio
import com.example.relatorios.R
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdificioForm(
    edificio: Edificio,
    onEdificioUpdated: (Edificio) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nome by remember { mutableStateOf(edificio.nome) }
    var endereco by remember { mutableStateOf(edificio.endereco) }
    var andares by remember { mutableStateOf(edificio.andares.toString()) }
    var apartamentosPorAndar by remember { mutableStateOf(edificio.apartamentosPorAndar.toString()) }
    var provedores by remember { mutableStateOf(edificio.provedores.toString()) }
    var nomesProvedores by remember { mutableStateOf(edificio.nomesProvedores.toMutableList()) }
    var condicaoShaft by remember { mutableStateOf(edificio.condicaoShaft) }
    var meioEntrada by remember { mutableStateOf(edificio.meioEntrada) }
    var tipoInstalacao by remember { mutableStateOf(edificio.tipoInstalacao) }
    var quantidadeBlocos by remember { mutableStateOf(edificio.quantidadeBlocos) }
    var expandedBlocos by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 2000.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do Edifício") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = endereco,
                onValueChange = { endereco = it },
                label = { Text("Endereço") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = andares,
                onValueChange = { andares = it },
                label = { Text("Número de Andares") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apartamentosPorAndar,
                onValueChange = { apartamentosPorAndar = it },
                label = { Text("Apartamentos por Andar") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de texto simples para número de provedores
            OutlinedTextField(
                value = provedores,
                onValueChange = { newValue ->
                    provedores = newValue
                    val numProvedores = newValue.toIntOrNull() ?: 0
                    Log.d("EdificioForm", "Número de provedores alterado para: $numProvedores")
                    nomesProvedores = MutableList(numProvedores) { "" }
                },
                label = { Text("Número de Provedores") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campos para nomes dos provedores - sempre visíveis para teste
            Text(
                text = "Nomes dos Provedores (Teste)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color.White
            )

            // Campo de teste fixo
            OutlinedTextField(
                value = "Campo de teste",
                onValueChange = { },
                label = { Text("Campo de Teste") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campos dinâmicos
            nomesProvedores.forEachIndexed { index, nome ->
                OutlinedTextField(
                    value = nome,
                    onValueChange = { newNome ->
                        nomesProvedores = nomesProvedores.toMutableList().also { it[index] = newNome }
                    },
                    label = { Text("Nome do Provedor ${index + 1}") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White.copy(alpha = 0.7f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = condicaoShaft,
                onValueChange = { condicaoShaft = it },
                label = { Text("Condição do Shaft") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = meioEntrada,
                onValueChange = { meioEntrada = it },
                label = { Text("Meio de Entrada") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tipoInstalacao,
                onValueChange = { tipoInstalacao = it },
                label = { Text("Tipo de Instalação") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown para quantidade de blocos
            ExposedDropdownMenuBox(
                expanded = expandedBlocos,
                onExpandedChange = { expandedBlocos = !expandedBlocos }
            ) {
                OutlinedTextField(
                    value = quantidadeBlocos.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Quantidade de Blocos") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBlocos)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedBlocos,
                    onDismissRequest = { expandedBlocos = false }
                ) {
                    (0..12).forEach { bloco ->
                        DropdownMenuItem(
                            text = { Text(bloco.toString()) },
                            onClick = {
                                quantidadeBlocos = bloco
                                expandedBlocos = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onEdificioUpdated(
                        edificio.copy(
                            nome = nome,
                            endereco = endereco,
                            andares = andares.toIntOrNull() ?: 0,
                            apartamentosPorAndar = apartamentosPorAndar.toIntOrNull() ?: 0,
                            provedores = provedores.toIntOrNull() ?: 0,
                            nomesProvedores = nomesProvedores,
                            condicaoShaft = condicaoShaft,
                            meioEntrada = meioEntrada,
                            tipoInstalacao = tipoInstalacao,
                            quantidadeBlocos = quantidadeBlocos
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }
}