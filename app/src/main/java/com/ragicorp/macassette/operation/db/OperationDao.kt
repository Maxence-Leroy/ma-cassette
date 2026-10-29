package com.ragicorp.macassette.operation.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationState
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

@Dao
interface OperationDao {
    @Transaction
    @Query("SELECT * FROM Operation ORDER BY date DESC, id DESC")
    fun getOperations(): Flow<List<OperationWithCategory>>

    @Transaction
    @Query("SELECT * FROM Operation WHERE date IN (:dates) AND state IN (:states)")
    suspend fun getOperations(
        dates: List<LocalDate>,
        states: List<OperationState>,
    ): List<OperationWithCategory>

    @Insert
    suspend fun insertOperation(operation: OperationEntity)

    @Insert
    suspend fun insertOperations(operations: List<OperationEntity>)

    @Update
    suspend fun updateOperation(operation: OperationEntity)

    @Delete
    suspend fun deleteOperation(operation: OperationEntity)

    @Query("SELECT * FROM Category ORDER BY CASE type WHEN 'Expense' THEN 0 WHEN 'Earning' THEN 1 ELSE 2 END, color, name COLLATE NOCASE")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM Parser ORDER BY name COLLATE NOCASE")
    fun getParsers(): Flow<List<ParserEntity>>

    @Insert
    suspend fun insertParser(parser: ParserEntity)

    @Delete
    suspend fun deleteParser(parser: ParserEntity)
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
