package com.tab.tablon.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tab.tablon.data.repository.SuggestionRepository

class ListViewModelFactory(
    private val suggestionRepository: SuggestionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListViewModel(suggestionRepository = suggestionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}