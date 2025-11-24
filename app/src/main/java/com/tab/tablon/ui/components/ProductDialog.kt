package com.tab.tablon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    suggestions: List<String>,
    lastUsedUnit: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Double, unit: String, description: String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onProductSelected: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("und") }
    var description by remember { mutableStateOf("") }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    val commonUnits = listOf("und", "pqt", "kg", "gr", "lt", "ml")

    // Efecto para la "memoria de unidades"
    LaunchedEffect(lastUsedUnit) {
        lastUsedUnit?.let {
            unit = it
        }
    }

    // Efecto para el autocompletado
    LaunchedEffect(name) {
        onSearchQueryChange(name)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Campo de Nombre con Autocompletado
                ExposedDropdownMenuBox(
                    expanded = suggestions.isNotEmpty() && name.isNotEmpty(),
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del Producto") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = suggestions.isNotEmpty() && name.isNotEmpty(),
                        onDismissRequest = { /* No hacer nada */ }
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    name = suggestion
                                    onProductSelected(suggestion) // Notifica al ViewModel
                                }
                            )
                        }
                    }
                }

                // Cantidad y Unidad en la misma fila
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically // Ayuda a alinear verticalmente
                ) {
                    // Campo de Cantidad
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad") },
                        // --- CAMBIO: Le damos un peso menor para que no domine ---
                        modifier = Modifier.weight(0.6f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true // Importante para evitar que se parta en varias líneas
                    )

                    // Menú desplegable para Unidades
                    ExposedDropdownMenuBox(
                        expanded = unitMenuExpanded,
                        onExpandedChange = { unitMenuExpanded = !unitMenuExpanded },
                        // --- CAMBIO: Le damos el peso restante ---
                        modifier = Modifier.weight(0.4f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                            // --- CAMBIO: El modificador va en el Box, no aquí ---
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            commonUnits.forEach { unitItem ->
                                DropdownMenuItem(
                                    text = { Text(unitItem) },
                                    onClick = {
                                        unit = unitItem
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Campo de Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (Opcional)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantityValue = quantity.toDoubleOrNull() ?: 1.0
                    onConfirm(name, quantityValue, unit, description)
                },
                enabled = name.isNotBlank() && quantity.isNotBlank()
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}