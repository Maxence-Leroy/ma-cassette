package com.ragicorp.macassette.operation.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.ragicorp.macassette.operation.Operation
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationDao {
    suspend fun getOperations(): List<Operation> = getOperationsWithCategory().map { it.toOperation() }

    @Transaction
    @Query("SELECT * FROM Operation")
    suspend fun getOperationsWithCategory(): List<OperationWithCategory>

    @Query("SELECT * FROM Category ORDER BY isEarning, color, name COLLATE NOCASE")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insertCategory(category: CategoryEntity)
}

data class OperationWithCategory(
    @Embedded val entity: OperationEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?,
) {
    fun toOperation() =
        Operation(
            id = entity.id,
            category = category?.toCategory(),
            date = entity.date,
            amount = entity.amount,
            label = entity.label,
            notes = entity.notes,
            recurrentPeriod = entity.recurrentPeriod,
            isWaitingForAction = entity.isWaitingForAction,
            state = entity.state,
        )
}
