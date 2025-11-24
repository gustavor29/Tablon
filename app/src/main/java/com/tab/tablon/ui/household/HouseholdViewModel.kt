package com.tab.tablon.ui.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tab.tablon.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HouseholdViewModel : ViewModel() {

    private val shoppingListRepository = ShoppingListRepository()

    private val _householdState = MutableStateFlow(HouseholdState())
    val householdState: StateFlow<HouseholdState> = _householdState.asStateFlow()

    fun createHousehold() {
        viewModelScope.launch {
            _householdState.update { it.copy(isLoading = true, error = null) }
            try {
                shoppingListRepository.createHousehold()
                _householdState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _householdState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun joinHousehold(code: String) {
        viewModelScope.launch {
            // Validamos que el código no esté en blanco
            if (code.isBlank()) {
                _householdState.update { it.copy(error = "El código no puede estar vacío") }
                return@launch
            }

            _householdState.update { it.copy(isLoading = true, error = null) }
            try {
                val householdId = shoppingListRepository.joinHousehold(code.trim().uppercase())
                if (householdId != null) {
                    _householdState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _householdState.update { it.copy(isLoading = false, error = "Código de hogar inválido") }
                }
            } catch (e: Exception) {
                _householdState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onErrorShown() {
        _householdState.update { it.copy(error = null) }
    }
}