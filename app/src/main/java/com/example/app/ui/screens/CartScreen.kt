package com.naturapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naturapp.ui.components.*
import com.naturapp.viewmodels.CartViewModel

// ══════════════════════════════════════════════════════════════════════════════
// PANTALLA: Carrito de Compras
// Equivale a app/(tabs)/cart.js
// VIEW del MVVM: delega toda la lógica al CartViewModel.
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun CartScreen(cartViewModel: CartViewModel) {
    val state   by cartViewModel.uiState.collectAsState()
    var address by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = NaturBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ── Título ─────────────────────────────────────────────────────
            Text(
                text       = "Mi Carrito (${state.items.size} items)",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = NaturNavy,
                modifier   = Modifier.padding(bottom = 16.dp)
            )

            if (state.items.isEmpty()) {
                // Carrito vacío
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = "Tu carrito está vacío",
                        fontSize = 16.sp,
                        color    = Color(0xFF999999)
                    )
                }
            } else {
                // ── Lista de items ─────────────────────────────────────────
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.items, key = { it.productId }) { item ->
                        CartItemRow(
                            item       = item,
                            onIncrease = {
                                cartViewModel.updateQuantity(
                                    item.productId, item.quantity + 1
                                )
                            },
                            onDecrease = {
                                cartViewModel.updateQuantity(
                                    item.productId, item.quantity - 1
                                )
                            },
                            onRemove   = { cartViewModel.removeItem(item.productId) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Footer: dirección, total y checkout ────────────────────
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(Modifier.height(12.dp))

                // Dirección de entrega
                OutlinedTextField(
                    value         = address,
                    onValueChange = { address = it },
                    label         = { Text("Dirección de entrega") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(8.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaturGreen,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Fila de total
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Total:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text       = "S/ ${"%.2f".format(state.total)}",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = NaturGreen
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Botón de checkout
                Button(
                    onClick  = {
                        cartViewModel.checkout(
                            address   = address,
                            onSuccess = { order ->
                                address = ""
                                /* Snackbar se maneja en el LaunchedEffect */
                            },
                            onError   = { msg ->
                                /* mostrar error */
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape   = RoundedCornerShape(12.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = NaturGreen),
                    enabled = !state.checkoutLoading
                ) {
                    if (state.checkoutLoading) {
                        CircularProgressIndicator(
                            color    = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text       = "Realizar Pedido",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
