package com.ragicorp.macassette.operation

import org.threeten.bp.LocalDate
import org.threeten.bp.Period

data class Operation(
    val id: Long,
    val category: Category? = null,
    val date: LocalDate,
    val amount: Double,
    val label: String,
    val notes: String,
    val recurrentPeriod: Period? = null,
    val isWaitingForAction: Boolean = false,
    val state: OperationState,
)

enum class OperationState {
    AutoImported,
    ManuallyAdded,
    Merged,
}
