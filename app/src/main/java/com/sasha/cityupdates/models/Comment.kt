package com.sasha.cityupdates.models

data class Comment(
    val id: String = "",
    val text: String = "",
    val postedBy: String = "",
    val userId: String = "",
    val timestamp: Long = 0L
)