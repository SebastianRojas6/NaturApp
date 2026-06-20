package com.naturapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturapp.models.CartItem
import com.naturapp.models.Product
import com.naturapp.models.Order
import com.naturapp.services.ApiService
import com.naturapp.services.DatabaseService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL — Gestión del Carrito de Compras
// Equivale a useCart.js (Custom Hook).
// Conecta View (CartScreen) con SQLite (local) y API REST (remota).
// Implementa validaciones de negocio antes de persistir.
// ══════════════════════════════════════════════════════════════════════════════

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val total: Double          = 0.0,
    val count: Int             = 0,
    val loading: Boolean       = false,
    val checkoutLoading: Boolean = false,
    val error: String?         = null,
    val lastOrder: Order?      = null
)

class CartViewModel(
    private val databaseService: DatabaseService,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        // Observar cambios en el carrito de forma reactiva (Flow de Room)
        viewModelScope.launch {
            databaseService.getCartItemsFlow().collect { entities ->
                val items = entities.map { entity ->
                    CartItem(
                        id        = entity.id,
                        productId = entity.productId,
                        name      = entity.name,
                        price     = entity.price,
                        image     = entity.image,
                        quantity  = entity.quantity
                    )
                }
                val total = databaseService.getCartTotal()
                val count = databaseService.getCartCount()
                _uiState.update { it.copy(items = items, total = total, count = count) }
            }
        }
    }

    // ── AGREGAR AL CARRITO ────────────────────────────────────────────────────

    /**
     * FLUJO DE INTEGRACIÓN (6 pasos):
     * (1) Evento UI → (2) ViewModel recibe acción → (3) Validación →
     * (4) SQLite insert → (5) Room notifica Flow → (6) UI se actualiza
     */
    fun addItem(product: Product, onError: (String) -> Unit) {
        viewModelScope.launch {
            // VALIDACIÓN: lógica de negocio — stock disponible
            if (!product.isAvailable()) {
                onError("Producto sin stock disponible")
                return@launch
            }
            try {
                databaseService.addToCart(product)
                // Room Flow notifica automáticamente; no necesitamos recargar manualmente
            } catch (e: Exception) {
                onError("Error al agregar al carrito")
            }
        }
    }

    // ── ACTUALIZAR CANTIDAD ───────────────────────────────────────────────────

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            // VALIDACIÓN: no permitir cantidades negativas
            if (quantity < 0) return@launch
            databaseService.updateCartQuantity(productId, quantity)
        }
    }

    // ── ELIMINAR ITEM ─────────────────────────────────────────────────────────

    fun removeItem(productId: String) {
        viewModelScope.launch {
            databaseService.removeFromCart(productId)
        }
    }

    // ── CHECKOUT ──────────────────────────────────────────────────────────────

    /**
     * CHECKOUT: Envía el pedido a la API remota y limpia el carrito local.
     * Integra persistencia remota (API) + persistencia local (SQLite).
     */
    fun checkout(address: String, onSuccess: (Order) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val items = _uiState.value.items

            // VALIDACIONES de lógica de negocio
            if (items.isEmpty()) {
                onError("El carrito está vacío")
                return@launch
            }
            if (address.isBlank()) {
                onError("Ingrese una dirección de entrega")
                return@launch
            }

            _uiState.update { it.copy(checkoutLoading = true) }
            try {
                // Construir payload para la API
                val orderData = JSONObject().apply {
                    put("items", JSONArray().apply {
                        items.forEach { item ->
                            put(JSONObject().apply {
                                put("productId", item.productId)
                                put("name", item.name)
                                put("price", item.price)
                                put("quantity", item.quantity)
                            })
                        }
                    })
                    put("total", _uiState.value.total)
                    put("address", address)
                }

                // CREATE en API remota (persistencia remota)
                val order = apiService.createOrder(orderData)

                // DELETE en SQLite local (limpiar carrito)
                databaseService.clearCart()

                _uiState.update { it.copy(checkoutLoading = false, lastOrder = order) }
                onSuccess(order)
            } catch (e: Exception) {
                _uiState.update { it.copy(checkoutLoading = false) }
                onError("Error al procesar el pedido: ${e.message}")
            }
        }
    }
}
