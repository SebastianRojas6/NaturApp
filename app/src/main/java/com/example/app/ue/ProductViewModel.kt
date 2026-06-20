package com.example.app.ue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.Product
import com.example.app.data.ProductApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val api = ProductApiService.create()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = api.searchProducts(query)
                _products.value = result.products.map {
                    Product(
                        id = it.code ?: "",
                        nombre = it.product_name ?: "Sin nombre",
                        caracteristicas = it.ingredients_text ?: "Sin descripción",
                        imagen = it.image_url
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}