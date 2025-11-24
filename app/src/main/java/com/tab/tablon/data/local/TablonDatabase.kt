package com.tab.tablon.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tab.tablon.data.model.LocalSuggestion

@Database(entities = [LocalSuggestion::class], version = 1, exportSchema = false)
abstract class TablonDatabase : RoomDatabase() {

    abstract fun suggestionDao(): SuggestionDao

    companion object {
        @Volatile
        private var INSTANCE: TablonDatabase? = null

        fun getDatabase(context: Context): TablonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TablonDatabase::class.java,
                    "tablon_local_database" // Nombre del archivo de la base de datos
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}