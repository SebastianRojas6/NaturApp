package com.naturapp.ui.screens

import androidx.compose.foundation.background
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
import com.naturapp.viewmodels.OrdersViewModel
import com.naturapp.viewmodels.ProfileViewModel

// ══════════════════════════════════════════════════════════════════════════════
// PANTALLA: Historial de Pedidos — equivale a app/(tabs)/orders.js
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun OrdersScreen(ordersViewModel: OrdersViewModel) {
    val state by ordersViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturBg)
            .padding(16.dp)
    ) {
        Text(
            text       = "Mis Pedidos",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = NaturNavy,
            modifier   = Modifier.padding(bottom = 16.dp)
        )

        when {
            state.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NaturGreen)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = NaturError)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { ordersViewModel.loadOrders() },
                            colors  = ButtonDefaults.buttonColors(containerColor = NaturGreen)
                        ) { Text("Reintentar") }
                    }
                }
            }
            state.orders.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes pedidos aún", color = Color(0xFF999999))
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.orders, key = { it.id }) { order ->
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text       = "Pedido #${order.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 16.sp,
                                        color      = NaturNavy
                                    )
                                    // Badge de estado con color dinámico del modelo
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(order.getStatusColor()),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text      = order.status.replaceFirstChar { it.uppercase() },
                                            color     = Color.White,
                                            fontSize  = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text     = order.getFormattedDate(),
                                    fontSize = 13.sp,
                                    color    = Color(0xFF888888)
                                )
                                Text(
                                    text       = "S/ ${"%.2f".format(order.total)}",
                                    fontSize   = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = NaturGreen,
                                    modifier   = Modifier.padding(top = 4.dp)
                                )
                                if (order.address.isNotEmpty()) {
                                    Text(
                                        text     = order.address,
                                        fontSize = 13.sp,
                                        color    = Color(0xFF666666),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PANTALLA: Perfil de Usuario — equivale a app/(tabs)/profile.js
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    val state by profileViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturBg)
            .padding(16.dp)
    ) {
        Text(
            text       = "Mi Perfil",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = NaturNavy,
            modifier   = Modifier.padding(bottom = 20.dp)
        )

        // ── Campos de perfil ───────────────────────────────────────────────
        OutlinedTextField(
            value         = state.name,
            onValueChange = { profileViewModel.setName(it) },
            label         = { Text("Nombre") },
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = NaturGreen)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value         = state.email,
            onValueChange = { profileViewModel.setEmail(it) },
            label         = { Text("Correo electrónico") },
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = NaturGreen)
        )
        Spacer(Modifier.height(12.dp))

        Button(
            onClick  = { profileViewModel.saveProfile() },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(containerColor = NaturGreen)
        ) { Text("Guardar Perfil") }

        if (state.saved) {
            Text(
                "✓ Perfil guardado",
                color    = NaturGreen,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // ── Preferencias (SharedPreferences / Persistencia Básica) ─────────
        Text("Preferencias", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NaturNavy)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Tema oscuro")
            Switch(
                checked         = state.darkTheme,
                onCheckedChange = { profileViewModel.toggleDarkTheme() },
                colors          = SwitchDefaults.colors(checkedThumbColor = NaturGreen,
                    checkedTrackColor = NaturGreen.copy(alpha = 0.4f))
            )
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Notificaciones")
            Switch(
                checked         = state.notifications,
                onCheckedChange = { profileViewModel.toggleNotifications() },
                colors          = SwitchDefaults.colors(checkedThumbColor = NaturGreen,
                    checkedTrackColor = NaturGreen.copy(alpha = 0.4f))
            )
        }

        Spacer(Modifier.weight(1f))

        // ── Botón de cerrar sesión ─────────────────────────────────────────
        OutlinedButton(
            onClick  = { profileViewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = NaturError),
            border   = ButtonDefaults.outlinedButtonBorder.copy()
        ) { Text("Cerrar sesión", color = NaturError) }
    }
}
