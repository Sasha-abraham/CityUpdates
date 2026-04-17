package com.sasha.cityupdates.models

data class Update(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val area: String = "",
    val postedBy: String = "",
    val userId: String = "",
    val timestamp: Long = 0L
)