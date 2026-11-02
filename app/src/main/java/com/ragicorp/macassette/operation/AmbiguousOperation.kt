package com.ragicorp.macassette.operation

// An imported operation matched by rules applying different categories, one of which has to be picked
data class AmbiguousOperation(
    val operation: Operation,
    val matchingCategories: List<Category>,
)
