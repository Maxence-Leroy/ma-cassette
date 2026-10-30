package com.ragicorp.macassette.operation

// A category with a budget and what is left of it for a month, negative once it is exceeded
data class Budget(
    val category: Category,
    val remaining: Double,
)
