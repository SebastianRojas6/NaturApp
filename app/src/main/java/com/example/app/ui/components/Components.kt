package com.naturapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.naturapp.models.CartItem
import com.naturapp.models.Product

// ══════════════════════════════════════════════════════════════════════════════
// COMPONENTES REUTILIZABLES — Capa de Presentación
// Equivalen a ProductCard.js, CartItemRow.js, CategoryChip.js
// ══════════════════════════════════════════════════════════════════════════════

val NaturGreen  = Color(0xFF148F77)
val NaturNavy   = Color(0xFF1A5276)
val NaturBg     = Color(0xFFF5F5F5)
val NaturError  = Color(0xFFE74C3C)

// ── ProductCard ───────────────────────────────────────────────────────────────

/**
 * Tarjeta de producto para la grilla del catálogo.
 * Equivale a ProductCard.js — solo renderiza, no conoce servicios.
 */
@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Imagen del producto
            AsyncImage(
                model              = product.image,
                contentDescription = product.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.height(8.dp))

            // Nombre del producto
            Text(
                text       = product.name,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF333333),
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )

            // Categoría
            Text(
                text     = product.category.replaceFirstChar { it.uppercase() },
                fontSize = 11.sp,
                color    = Color(0xFF888888)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier       = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Precio formateado
                Text(
                    text       = product.getFormattedPrice(),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = NaturGreen
                )

                // Botón "+" para agregar al carrito
                Box(
                    modifier         = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NaturGreen)
                        .clickable(enabled = product.isAvailable()) { onAddToCart() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Add,
                        contentDescription = "Agregar al carrito",
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // Indicador de sin stock
            if (!product.isAvailable()) {
                Text(
                    text      = "Sin stock",
                    fontSize  = 11.sp,
                    color     = NaturError,
                    modifier  = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ── CategoryChip ──────────────────────────────────────────────────────────────

/**
 * Chip de categoría para el filtro horizontal.
 * Equivale a CategoryChip.js
 */
@Composable
fun CategoryChip(
    label: String,
    active: Boolean,
    onPress: () -> Unit
) {
    val bg   = if (active) NaturGreen else Color.White
    val text = if (active) Color.White else Color(0xFF555555)

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onPress() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text       = label.replaceFirstChar { it.uppercase() },
            color      = text,
            fontSize   = 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ── CartItemRow ───────────────────────────────────────────────────────────────

/**
 * Fila de item en el carrito con controles de cantidad.
 * Equivale a CartItemRow.js
 */
@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape     = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            AsyncImage(
                model              = item.image,
                contentDescription = item.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.name,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text      = item.getFormattedSubtotal(),
                    fontSize  = 15.sp,
                    color     = NaturGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            // Controles de cantidad
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, "Disminuir",
                        tint = NaturNavy, modifier = Modifier.size(18.dp))
                }
                Text(
                    text       = "${item.quantity}",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "Aumentar",
                        tint = NaturGreen, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Eliminar",
                        tint = NaturError, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
