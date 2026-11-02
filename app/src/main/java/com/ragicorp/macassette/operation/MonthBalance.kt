package com.ragicorp.macassette.operation

import org.threeten.bp.YearMonth

// Expenses are negative and earnings positive, so their sum is what was saved over the month (negative when overspent)
data class MonthBalance(
    val month: YearMonth,
    val expenses: Double,
    val earnings: Double,
) {
    val balance = expenses + earnings
}
