package com.ragicorp.macassette.operation

// Operations that several rules could categorize aren't saved, so their category can be picked first
data class ImportResult(
    val importedCount: Int,
    val ambiguousOperations: List<AmbiguousOperation>,
)
