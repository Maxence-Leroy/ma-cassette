package com.ragicorp.macassette.operation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "operation_category_cross_ref",
    primaryKeys = ["operationId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = Operation::class,
            parentColumns = ["id"],
            childColumns = ["operationId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId")],
)
data class OperationCategoryCrossRef(
    val operationId: Long,
    val categoryId: Long,
)
