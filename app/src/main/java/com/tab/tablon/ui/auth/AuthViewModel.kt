package com.tab.tablon.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tab.tablon.data.repository.AuthRepository
import com.tab.tablon.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.tab.tablon.data.secure.CredentialsManager
import androidx.lifecycle.ViewModelProvider

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val shoppingListRepository = ShoppingListRepository()
    private val credentialsManager = CredentialsManager(application) // Creamos la instancia

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.registerWithEmail(email, password)

            if (result.isSuccess) {
                // Después de un registro exitoso, siempre vamos al onboarding
                _authState.update {
                    it.copy(isLoading = false, navigation = AuthNavigation.GO_TO_ONBOARDING)
                }
            } else {
                handleAuthError(result.exceptionOrNull())
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.loginWithEmail(email, password)

            if (result.isSuccess) {
                // --- Guardamos las credenciales al iniciar sesión ---
                credentialsManager.saveCredentials(email, password)
                // Si el login es exitoso, comprobamos si el usuario tiene un hogar
                checkUserHousehold()
            } else {
                handleAuthError(result.exceptionOrNull())
            }
        }
    }

    /**
     * Comprueba los datos del usuario en Firestore para decidir a dónde navegar.
     */
    private suspend fun checkUserHousehold() {
        val user = authRepository.getUserData()
        if (user?.householdId != null) {
            // El usuario ya tiene un hogar, vamos a la lista de compras
            _authState.update {
                it.copy(isLoading = false, navigation = AuthNavigation.GO_TO_HOME_SCREEN)
            }
        } else {
            // El usuario no tiene hogar, vamos al onboarding
            _authState.update {
                it.copy(isLoading = false, navigation = AuthNavigation.GO_TO_ONBOARDING)
            }
        }
    }

    /**
     * Función de ayuda para manejar los errores de autenticación.
     */
    private fun handleAuthError(exception: Throwable?) {
        val errorMessage = exception?.message ?: "Error desconocido"
        _authState.update { it.copy(isLoading = false, error = errorMessage) }
    }

    /**
     * Resetea el evento de navegación para que no se dispare de nuevo (ej. al rotar la pantalla).
     */
    fun onNavigationHandled() {
        _authState.update { it.copy(navigation = null) }
    }
}


class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}