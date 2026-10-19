package com.ragicorp.macassette.operation

data class Category(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val budget: Double? = null,
    val isEarning: Boolean,
)
