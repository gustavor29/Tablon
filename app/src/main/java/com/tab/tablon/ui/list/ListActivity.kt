package com.tab.tablon.ui.list

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tab.tablon.TablonApplication
import com.tab.tablon.data.model.Product
import com.tab.tablon.ui.components.ProductDialog
import com.tab.tablon.ui.theme.TablonTheme

class ListActivity : ComponentActivity() {

    private val listViewModel: ListViewModel by viewModels {
        ListViewModelFactory((application as TablonApplication).suggestionRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TablonTheme {
                val uiState by listViewModel.uiState.collectAsState()

                ListScreen(
                    state = uiState,
                    onAddProduct = listViewModel::addProduct,
                    onProductUpdate = listViewModel::updateProduct,
                    onProductRemove = listViewModel::removeProduct,
                    onSearchQueryChange = listViewModel::onSearchQueryChange,
                    onProductSelected = listViewModel::onProductSelected,
                    onArchiveList = listViewModel::archiveCurrentList
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    state: ListUiState,
    onAddProduct: (String, Double, String, String) -> Unit,
    onProductUpdate: (Product) -> Unit,
    onProductRemove: (Product) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onProductSelected: (String) -> Unit,
    onArchiveList: () -> Unit
) {
    Log.d("ListScreen", "Recomponiendo ListScreen. isLoading: ${state.isLoading}, Products: ${state.products.size}")
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    if (showAddProductDialog) {
        ProductDialog(
            suggestions = state.suggestions,
            lastUsedUnit = state.lastUsedUnit,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, qty, unit, desc ->
                onAddProduct(name, qty, unit, desc)
                showAddProductDialog = false
            },
            onSearchQueryChange = onSearchQueryChange,
            onProductSelected = onProductSelected
        )
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text("Archivar Lista") },
            text = { Text("¿Estás seguro de que quieres finalizar la compra y archivar esta lista? La lista activa se vaciará.") },
            confirmButton = {
                Button(onClick = {
                    onArchiveList()
                    showArchiveDialog = false
                }) { Text("Sí, archivar") }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Lista de Compras",
                        modifier = if (state.products.isNotEmpty()) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(onLongPress = { showArchiveDialog = true })
                            }
                        } else Modifier
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Abrir Menú Hamburguesa */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddProductDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
            }
        }
    ) { paddingValues ->
        // --- CAMBIO: Hemos simplificado la estructura y movido el padding ---
        // El Box ya no es necesario, LazyColumn puede manejar el padding y el tamaño.

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val (toBuy, purchased) = state.products.partition { !it.purchased }
            LazyColumn(
                // El padding del Scaffold se aplica aquí
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ListSectionHeader(title = "Por Comprar")
                }
                if (toBuy.isEmpty()) {
                    item { EmptyState(message = "¡Nada por comprar! Añade un producto.") }
                } else {
                    items(toBuy, key = { it.id }) { product ->
                        ProductItem(product = product, onUpdate = onProductUpdate, onRemove = onProductRemove)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ListSectionHeader(title = "En el Carrito")
                }
                if (purchased.isEmpty()) {
                    item { EmptyState(message = "Aún no has comprado nada de la lista.") }
                } else {
                    items(purchased, key = { it.id }) { product ->
                        ProductItem(product = product, onUpdate = onProductUpdate, onRemove = onProductRemove)
                    }
                }
            }
        }
    }
}

@Composable
fun ListSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold
    )
}
@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, fontStyle = FontStyle.Italic, color = Color.Gray)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductItem(
    product: Product,
    onUpdate: (Product) -> Unit,
    onRemove: (Product) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    onUpdate(product.copy(purchased = !product.purchased))
                },
                onLongClick = {
                    showMenu = true
                }
            ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (product.purchased) TextDecoration.LineThrough else null
                )
                Text(
                    text = "${product.quantity} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textDecoration = if (product.purchased) TextDecoration.LineThrough else null
                )
                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        textDecoration = if (product.purchased) TextDecoration.LineThrough else null
                    )
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Eliminar de la lista") },
                    onClick = {
                        onRemove(product)
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview() {
    TablonTheme {
        val products = listOf(
            Product(id = "1", name = "Leche", quantity = 2.0, unit = "lt", purchased = false),
            Product(id = "2", name = "Pan", quantity = 1.0, unit = "und", description = "De molde", purchased = false),
            Product(id = "3", name = "Huevos", quantity = 12.0, unit = "und", purchased = true)
        )
        ListScreen(
            state = ListUiState(isLoading = false, products = products),
            onAddProduct = { _, _, _, _ -> },
            onProductUpdate = {},

            onProductRemove = {},
            onSearchQueryChange = {},
            onProductSelected = {},
            onArchiveList = {}
        )
    }
}