package com.tab.tablon.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val quantity: Double = 1.0,
    val unit: String = "und",
    val description: String = "",
    val purchased: Boolean = false
)