package com.ragicorp.macassette.operation

import org.threeten.bp.LocalDate

// Criteria an operation must all meet to be listed. No category id and null dates don't filter anything.
data class OperationFilter(
    val categoryIds: Set<Long>,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val isUncategorizedOnly: Boolean,
    val isWaitingForActionOnly: Boolean,
)
