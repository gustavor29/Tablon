package com.tab.tablon.ui.auth

// Enum para representar el resultado de la navegación
enum class AuthNavigation {
    GO_TO_HOME_SCREEN, // El usuario ya tiene un hogar, ir a la lista
    GO_TO_ONBOARDING   // El usuario es nuevo, ir a la pantalla de crear/unirse a hogar
}

data class AuthState(
    val isLoading: Boolean = false,
    val navigation: AuthNavigation? = null, // El nuevo campo para la navegación
    val error: String? = null
)