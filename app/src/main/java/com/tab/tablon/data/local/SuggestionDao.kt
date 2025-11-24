package com.tab.tablon.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.tab.tablon.data.model.LocalSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDao {

    // 'Upsert' es una operación inteligente: si el producto ya existe, lo actualiza;
    // si no existe, lo inserta. Perfecto para nuestra "memoria" de unidades.
    @Upsert
    suspend fun saveSuggestion(suggestion: LocalSuggestion)

    // Obtiene una lista de nombres de productos que empiezan con el texto que el usuario está escribiendo.
    // Usamos '||' para concatenar el símbolo '%' que en SQL significa "cualquier cadena de caracteres".
    // LIMIT 10 para no sobrecargar la UI con demasiadas sugerencias.
    @Query("SELECT productName FROM suggestions WHERE productName LIKE :query || '%' LIMIT 10")
    fun getSuggestions(query: String): Flow<List<String>>

    // Obtiene la última unidad usada para un producto específico.
    @Query("SELECT lastUsedUnit FROM suggestions WHERE productName = :productName")
    suspend fun getLastUsedUnitFor(productName: String): String? // Puede ser nulo si nunca se ha usado
}