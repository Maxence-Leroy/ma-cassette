package com.ragicorp.macassette.operation.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.CategoryType

// Table name pinned to the pre-split class name so the schema stays unchanged
@Entity(tableName = "Category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val budget: Double? = null,
    val type: CategoryType,
) {
    fun toCategory() =
        Category(
            id = id,
            name = name,
            color = color,
            budget = budget,
            type = type,
        )
}

fun Category.toEntity() =
    CategoryEntity(
        id = id,
        name = name,
        color = color,
        budget = budget,
        type = type,
    )
