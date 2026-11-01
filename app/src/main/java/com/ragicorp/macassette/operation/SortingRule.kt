package com.ragicorp.macassette.operation

data class SortingRule(
    val id: Long = 0,
    val textToRecognize: String,
    val categoryToApply: Category,
)
