package com.naturapp.models

/**
 * CAPA MODELO — Entidad CartItem
 * Representa un producto dentro del carrito de compras.
 * Se persiste en SQLite (Room).
 */
data class CartItem(
    val id: Long = 0,
    val productId: String,
    val name: String,
    val price: Double,
    val image: String,
    val quantity: Int = 1
) {
    /** Calcula el subtotal de este item */
    fun getSubtotal(): Double = price * quantity

    /** Subtotal formateado en soles */
    fun getFormattedSubtotal(): String {
        return "S/ ${"%.2f".format(getSubtotal())}"
    }
}
