package com.naturapp.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CAPA MODELO — Entidad Order
 * Representa un pedido completado y enviado a la API remota.
 */
data class Order(
    val id: String,
    val items: List<CartItem> = emptyList(),
    val total: Double,
    val status: String = "pendiente",
    val date: String = "",
    val address: String = ""
) {
    /** Fecha formateada en español (Perú) */
    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date: Date = isoFormat.parse(date) ?: return date
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")).format(date)
        } catch (e: Exception) {
            date
        }
    }

    /** Color de estado según la etapa del pedido */
    fun getStatusColor(): Long {
        return when (status) {
            "pendiente"   -> 0xFFF39C12
            "procesando"  -> 0xFF3498DB
            "enviado"     -> 0xFF8E44AD
            "entregado"   -> 0xFF27AE60
            else          -> 0xFF95A5A6
        }
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Order {
            return Order(
                id      = map["id"]?.toString() ?: "",
                total   = (map["total"] as? Number)?.toDouble() ?: 0.0,
                status  = map["status"]?.toString() ?: "pendiente",
                date    = map["date"]?.toString() ?: "",
                address = map["address"]?.toString() ?: ""
            )
        }
    }
}
