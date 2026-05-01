package com.sasha.cityupdates.models

data class Update(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val area: String = "",
    val postedBy: String = "",
    val userId: String = "",
    val timestamp: Long = 0L,
    val urgency: String = "Low",
    val flagCount: Int = 0,
    val flaggedBy: List<String> = emptyList(),
    val upvoteCount: Int = 0,
    val upvotedBy: List<String> = emptyList(),
    val isResolved: Boolean = false,
    val commentCount: Int = 0
)