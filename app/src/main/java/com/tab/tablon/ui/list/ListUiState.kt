package com.tab.tablon.ui.list

import com.tab.tablon.data.model.Product

data class ListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val lastUsedUnit: String? = null
)