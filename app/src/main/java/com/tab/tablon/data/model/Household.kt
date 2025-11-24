package com.tab.tablon.data.model

data class Household(
    val id: String = "",
    val name: String = "Mi Hogar",
    val invitationCode: String = "",
    val members: List<String> = emptyList(),
    val activeList: List<Product> = emptyList()
)