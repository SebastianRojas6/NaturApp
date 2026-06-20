package com.naturapp.models

import java.text.NumberFormat
import java.util.Locale

/**
 * CAPA MODELO — Entidad Producto
 * Equivale al 'Model' del patrón MVVM.
 * Encapsula datos y lógica de dominio del producto.
 */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: String,
    val category: String,
    val stock: Int,
    val rating: Double = 0.0,
    val benefits: List<String> = emptyList()
) {
    /** Verifica si hay stock disponible */
    fun isAvailable(): Boolean = stock > 0

    /** Formato de precio en soles peruanos para mostrar en la UI */
    fun getFormattedPrice(): String {
        val format = NumberFormat.getNumberInstance(Locale("es", "PE"))
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        return "S/ ${format.format(price)}"
    }

    companion object {
        /** Factory method: crea instancia desde Map (JSON deserializado) */
        fun fromMap(map: Map<String, Any?>): Product {
            return Product(
                id          = map["id"]?.toString() ?: "",
                name        = map["name"]?.toString() ?: "",
                description = map["description"]?.toString() ?: "",
                price       = (map["price"] as? Number)?.toDouble() ?: 0.0,
                image       = map["image"]?.toString() ?: "",
                category    = map["category"]?.toString() ?: "",
                stock       = (map["stock"] as? Number)?.toInt() ?: 0,
                rating      = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                benefits    = (map["benefits"] as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}
