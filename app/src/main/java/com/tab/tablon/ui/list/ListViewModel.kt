package com.tab.tablon.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tab.tablon.data.model.Product
import com.tab.tablon.data.repository.AuthRepository
import com.tab.tablon.data.repository.ShoppingListRepository
import com.tab.tablon.data.repository.SuggestionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ListViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val shoppingListRepository: ShoppingListRepository = ShoppingListRepository(),
    private val suggestionRepository: SuggestionRepository
) : ViewModel() {

    // VOLVEMOS A UN ÚNICO STATE PARA SIMPLIFICAR
    private val _uiState = MutableStateFlow(ListUiState(isLoading = true))
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private var householdId: String? = null

    init {
        initializeAndObserveList()
    }

    private fun initializeAndObserveList() {
        viewModelScope.launch {
            Log.d("ListViewModel", "Iniciando. Obteniendo datos del usuario...")
            val user = authRepository.getUserData()
            householdId = user?.householdId
            Log.d("ListViewModel", "HouseholdId obtenido: $householdId")

            if (householdId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo encontrar el hogar del usuario.") }
                return@launch
            }

            shoppingListRepository.getActiveShoppingList(householdId!!)
                .catch { exception ->
                    Log.e("ListViewModel", "Error en el Flow de la lista", exception)
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
                .collect { productList ->
                    Log.d("ListViewModel", "Flow ha emitido una nueva lista con ${productList.size} productos.")
                    _uiState.update { it.copy(isLoading = false, products = productList) }
                }
        }
    }

    fun addProduct(name: String, quantity: Double, unit: String, description: String) {
        householdId ?: return
        viewModelScope.launch {
            val newProduct = Product(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                quantity = quantity,
                unit = unit,
                description = description.trim()
            )
            shoppingListRepository.addProduct(householdId!!, newProduct)
            suggestionRepository.saveSuggestion(name, unit)
        }
    }

    fun updateProduct(product: Product) {
        householdId ?: return
        viewModelScope.launch {
            shoppingListRepository.updateProduct(householdId!!, product)
        }
    }

    fun removeProduct(product: Product) {
        householdId ?: return
        viewModelScope.launch {
            shoppingListRepository.removeProduct(householdId!!, product)
        }
    }

    fun archiveCurrentList() {
        // --- LÍNEA CORREGIDA ---
        val currentProducts = uiState.value.products
        householdId ?: return
        if (currentProducts.isEmpty()) return

        viewModelScope.launch {
            shoppingListRepository.archiveCurrentList(householdId!!, currentProducts)
        }
    }

    fun onSearchQueryChange(query: String) {
        viewModelScope.launch {
            if (query.length > 1) {
                suggestionRepository.getSuggestions(query)
                    .distinctUntilChanged()
                    .collect { suggestions ->
                        _uiState.update { it.copy(suggestions = suggestions) }
                    }
            } else {
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
        }
    }
    fun onProductSelected(productName: String) {
        viewModelScope.launch {
            val unit = suggestionRepository.getLastUsedUnitFor(productName)
            _uiState.update { it.copy(lastUsedUnit = unit, suggestions = emptyList()) }
            kotlinx.coroutines.delay(200)
            _uiState.update { it.copy(lastUsedUnit = null) }
        }
    }
}