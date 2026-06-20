package com.naturapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturapp.models.Order
import com.naturapp.services.ApiService
import com.naturapp.services.StorageService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL — Historial de Pedidos
// Equivale a useOrders.js
// ══════════════════════════════════════════════════════════════════════════════

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val loading: Boolean    = false,
    val error: String?      = null
)

class OrdersViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init { loadOrders() }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val orders = apiService.getOrders()
                _uiState.update { it.copy(orders = orders, loading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = "No se pudo cargar el historial")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL — Perfil de Usuario
// Equivale a useProfile.js
// Trabaja exclusivamente con SharedPreferences (persistencia básica).
// ══════════════════════════════════════════════════════════════════════════════

data class ProfileUiState(
    val name: String           = "",
    val email: String          = "",
    val darkTheme: Boolean     = false,
    val notifications: Boolean = true,
    val saved: Boolean         = false
)

class ProfileViewModel(
    private val storageService: StorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        _uiState.update {
            it.copy(
                name          = storageService.getUserName(),
                email         = storageService.getUserEmail(),
                darkTheme     = storageService.isDarkTheme(),
                notifications = storageService.getNotifications()
            )
        }
    }

    fun setName(name: String)   = _uiState.update { it.copy(name = name, saved = false) }
    fun setEmail(email: String) = _uiState.update { it.copy(email = email, saved = false) }

    fun saveProfile() {
        viewModelScope.launch {
            storageService.saveUserProfile(
                _uiState.value.name,
                _uiState.value.email
            )
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun toggleDarkTheme() {
        val newVal = !_uiState.value.darkTheme
        _uiState.update { it.copy(darkTheme = newVal) }
        storageService.setDarkTheme(newVal)
    }

    fun toggleNotifications() {
        val newVal = !_uiState.value.notifications
        _uiState.update { it.copy(notifications = newVal) }
        storageService.setNotifications(newVal)
    }

    fun logout() {
        storageService.logout()
        _uiState.update { ProfileUiState() }
    }
}
