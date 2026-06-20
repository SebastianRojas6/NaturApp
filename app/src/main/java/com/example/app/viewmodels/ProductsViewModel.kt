package com.naturapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturapp.models.Product
import com.naturapp.services.ApiService
import com.naturapp.services.StorageService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL — Gestión de Productos
// Equivale a useProducts.js (Custom Hook).
// Conecta la View (ProductsScreen) con la capa de datos (ApiService).
// Usa StateFlow en lugar de useState de React.
// ══════════════════════════════════════════════════════════════════════════════

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val loading: Boolean        = false,
    val error: String?          = null,
    val category: String        = "todos",
    val searchQuery: String     = ""
)

class ProductsViewModel(
    private val apiService: ApiService,
    private val storageService: StorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        // Restaurar última categoría al iniciar (persistencia básica)
        val lastCategory = storageService.getLastCategory()
        _uiState.update { it.copy(category = lastCategory) }
        loadProducts()
    }

    /** Carga productos desde la API según la categoría seleccionada */
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val cat = _uiState.value.category.takeIf { it != "todos" }
                val products = apiService.getProducts(cat)
                _uiState.update { it.copy(products = products, loading = false) }

                // Guardar última categoría en SharedPreferences (persistencia básica)
                storageService.saveLastCategory(_uiState.value.category)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = "No se pudieron cargar los productos")
                }
            }
        }
    }

    /** Cambia la categoría seleccionada y recarga productos */
    fun setCategory(category: String) {
        _uiState.update { it.copy(category = category) }
        loadProducts()
    }

    /** Actualiza la query de búsqueda en el estado */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** Busca productos por texto en la API */
    fun search(query: String) {
        if (query.isBlank()) {
            loadProducts()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val results = apiService.searchProducts(query)
                _uiState.update { it.copy(products = results, loading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = "Error en la búsqueda") }
            }
        }
    }
}
