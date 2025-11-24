package com.tab.tablon.ui.household

data class HouseholdState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false, // Para saber cuándo navegar a la lista
    val error: String? = null
)