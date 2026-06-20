package com.naturapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naturapp.ui.components.*
import com.naturapp.viewmodels.ProductsViewModel
import com.naturapp.viewmodels.CartViewModel

// ══════════════════════════════════════════════════════════════════════════════
// PANTALLA: Home — Catálogo de Productos
// Equivale a app/(tabs)/home.js
// VIEW del patrón MVVM: solo renderiza UI y delega al ViewModel.
// NUNCA importa servicios de datos directamente.
// ══════════════════════════════════════════════════════════════════════════════

val CATEGORIES = listOf(
    "todos", "superfoods", "aceites",
    "capsulas", "infusiones", "miel"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    productsViewModel: ProductsViewModel,
    cartViewModel: CartViewModel,
    onNavigateToProduct: (String) -> Unit
) {
    // Observar el estado del ViewModel (equivale a desestructurar el Custom Hook)
    val state by productsViewModel.uiState.collectAsState()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar Snackbar al agregar al carrito
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NaturBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // ── Barra de búsqueda ──────────────────────────────────────────
            OutlinedTextField(
                value         = state.searchQuery,
                onValueChange = { productsViewModel.setSearchQuery(it) },
                placeholder   = { Text("Buscar productos naturales...") },
                leadingIcon   = {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = NaturGreen)
                },
                trailingIcon  = {
                    if (state.searchQuery.isNotEmpty()) {
                        TextButton(onClick = { productsViewModel.search(state.searchQuery) }) {
                            Text("Buscar", color = NaturGreen,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                },
                modifier      = Modifier.fillMaxWidth(),
                shape         = MaterialTheme.shapes.medium,
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = NaturGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(Modifier.height(10.dp))

            // ── Chips de categorías ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp)
            ) {
                CATEGORIES.forEach { cat ->
                    CategoryChip(
                        label   = cat,
                        active  = state.category == cat,
                        onPress = { productsViewModel.setCategory(cat) }
                    )
                }
            }

            // ── Contenido principal ────────────────────────────────────────
            when {
                state.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NaturGreen)
                    }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = NaturError, fontSize = 16.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { productsViewModel.loadProducts() },
                                colors  = ButtonDefaults.buttonColors(containerColor = NaturGreen)
                            ) { Text("Reintentar") }
                        }
                    }
                }
                state.products.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay productos disponibles", color = Color(0xFF999999))
                    }
                }
                else -> {
                    // Grilla de productos (2 columnas) con pull-to-refresh
                    LazyVerticalGrid(
                        columns      = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            ProductCard(
                                product    = product,
                                onAddToCart = {
                                    // EVENTO UI → ViewModel → Validación → SQLite → UI
                                    cartViewModel.addItem(
                                        product  = product,
                                        onError  = { msg -> snackbarMessage = msg }
                                    )
                                    snackbarMessage = "${product.name} agregado al carrito"
                                },
                                onNavigate  = { onNavigateToProduct(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
