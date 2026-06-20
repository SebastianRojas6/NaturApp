package com.naturapp.services

import android.content.Context
import androidx.core.content.edit
class StorageService(context: Context) {

    private val prefs = context.getSharedPreferences(
        "naturapp_prefs", Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_USER_NAME      = "user_name"
        private const val KEY_USER_EMAIL     = "user_email"
        private const val KEY_AUTH_TOKEN     = "auth_token"
        private const val KEY_DARK_THEME     = "dark_theme"
        private const val KEY_NOTIFICATIONS  = "notifications"
        private const val KEY_LAST_CATEGORY  = "last_category"
        private const val KEY_ONBOARDING     = "onboarding_done"
    }

    // ── PERFIL DE USUARIO ─────────────────────────────────────────────────────

    fun saveUserProfile(name: String, email: String) {
        prefs.edit {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
        }
    }

    fun getUserName(): String  = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    // ── TOKEN DE AUTENTICACIÓN ────────────────────────────────────────────────

    fun saveToken(token: String) = prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    fun getToken(): String?      = prefs.getString(KEY_AUTH_TOKEN, null)
    fun clearToken()             = prefs.edit { remove(KEY_AUTH_TOKEN) }

    // ── PREFERENCIAS BOOLEANAS ────────────────────────────────────────────────

    fun setDarkTheme(enabled: Boolean) = prefs.edit { putBoolean(KEY_DARK_THEME, enabled) }
    fun isDarkTheme(): Boolean          = prefs.getBoolean(KEY_DARK_THEME, false)

    fun setNotifications(enabled: Boolean) = prefs.edit { putBoolean(KEY_NOTIFICATIONS, enabled) }
    fun getNotifications(): Boolean         = prefs.getBoolean(KEY_NOTIFICATIONS, true)

    // ── ÚLTIMA CATEGORÍA VISITADA ─────────────────────────────────────────────

    fun saveLastCategory(category: String) = prefs.edit { putString(KEY_LAST_CATEGORY, category) }
    fun getLastCategory(): String           = prefs.getString(KEY_LAST_CATEGORY, "todos") ?: "todos"

    // ── ONBOARDING ────────────────────────────────────────────────────────────

    fun setOnboardingDone() = prefs.edit { putBoolean(KEY_ONBOARDING, true) }
    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING, false)

    // ── CERRAR SESIÓN ─────────────────────────────────────────────────────────

    fun logout() {
        prefs.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
        }
    }
}
