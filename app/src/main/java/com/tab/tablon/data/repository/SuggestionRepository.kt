package com.tab.tablon.data.repository

import com.tab.tablon.data.local.SuggestionDao
import com.tab.tablon.data.model.LocalSuggestion
import kotlinx.coroutines.flow.Flow

class SuggestionRepository(private val suggestionDao: SuggestionDao) {

    /**
     * Guarda o actualiza una sugerencia en la base de datos local.
     * Se llamará a esta función cada vez que se añada un nuevo producto a la lista de compras.
     * @param productName El nombre del producto.
     * @param unit La unidad utilizada para ese producto.
     */
    suspend fun saveSuggestion(productName: String, unit: String) {
        // Normalizamos el nombre del producto para evitar duplicados como "Pan" y "pan".
        // Lo guardamos en minúsculas y sin espacios extra.
        val cleanedName = productName.trim().lowercase()
        if (cleanedName.isNotEmpty()) {
            val suggestion = LocalSuggestion(productName = cleanedName, lastUsedUnit = unit)
            suggestionDao.saveSuggestion(suggestion)
        }
    }

    /**
     * Obtiene un flujo de sugerencias de nombres de productos que coinciden con la consulta del usuario.
     * @param query El texto que el usuario está escribiendo.
     * @return Un Flow que emite una lista de nombres de productos sugeridos.
     */
    fun getSuggestions(query: String): Flow<List<String>> {
        // Limpiamos la consulta de la misma forma que guardamos los nombres.
        val cleanedQuery = query.trim().lowercase()
        return suggestionDao.getSuggestions(cleanedQuery)
    }

    /**
     * Obtiene la última unidad utilizada para un producto específico.
     * @param productName El nombre completo del producto seleccionado.
     * @return La unidad como un String, o null si no se encuentra.
     */
    suspend fun getLastUsedUnitFor(productName: String): String? {
        val cleanedName = productName.trim().lowercase()
        return suggestionDao.getLastUsedUnitFor(cleanedName)
    }
}