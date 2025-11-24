package com.tab.tablon

import android.app.Application // <-- Importante añadir esta línea
import com.tab.tablon.data.local.TablonDatabase
import com.tab.tablon.data.repository.SuggestionRepository

// Se declara como una 'class' que 'hereda de' Application
class TablonApplication : Application() {
    private val database by lazy { TablonDatabase.getDatabase(this) }
    val suggestionRepository by lazy { SuggestionRepository(database.suggestionDao()) }
}