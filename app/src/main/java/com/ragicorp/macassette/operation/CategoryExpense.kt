package com.ragicorp.macassette.operation

// What was spent in an expense category over a period, refunds deducted. Null category for uncategorized operations.
data class CategoryExpense(
    val category: Category?,
    val amount: Double,
)
