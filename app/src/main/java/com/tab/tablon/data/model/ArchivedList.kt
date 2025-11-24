package com.tab.tablon.data.model

import com.google.firebase.Timestamp

data class ArchivedList(
    val id: String = "",
    val archivedDate: Timestamp = Timestamp.now(),
    val products: List<Product> = emptyList(),
    val householdId: String = ""
)