package com.ragicorp.macassette.operation

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

@Dao
interface OperationDao {
    suspend fun getOperations(): List<Operation> = getOperationsWithCategory().map { it.toOperation() }

    @Transaction
    @Query("SELECT * FROM OperationEntity")
    suspend fun getOperationsWithCategory(): List<OperationWithCategory>
}

data class OperationWithCategory(
    @Embedded val entity: OperationEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category?,
) {
    fun toOperation() =
        Operation(
            id = entity.id,
            category = category,
            date = entity.date,
            amount = entity.amount,
            label = entity.label,
            notes = entity.notes,
            recurrentPeriod = entity.recurrentPeriod,
            isWaitingForAction = entity.isWaitingForAction,
            state = entity.state,
        )
}
