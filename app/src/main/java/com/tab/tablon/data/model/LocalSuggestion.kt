package com.tab.tablon.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggestions")
data class LocalSuggestion(
    @PrimaryKey
    val productName: String,
    val lastUsedUnit: String
)