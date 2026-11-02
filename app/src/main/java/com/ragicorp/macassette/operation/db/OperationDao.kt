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
    // A null bound isn't applied, and each other criterion only filters when its flag is set
    @Transaction
    @Query(
        "SELECT * FROM Operation WHERE (:isCategoryFiltered = 0 OR categoryId IN (:categoryIds)) " +
            "AND (:start IS NULL OR date >= :start) AND (:end IS NULL OR date <= :end) " +
            "AND (:isUncategorizedOnly = 0 OR categoryId IS NULL) AND (:isWaitingForActionOnly = 0 OR isWaitingForAction = 1) " +
            "ORDER BY date DESC, id DESC",
    )
    fun getOperations(
        isCategoryFiltered: Boolean,
        categoryIds: List<Long>,
        start: LocalDate?,
        end: LocalDate?,
        isUncategorizedOnly: Boolean,
        isWaitingForActionOnly: Boolean,
    ): Flow<List<OperationWithCategory>>

    @Transaction
    @Query("SELECT * FROM Operation WHERE date IN (:dates) AND state IN (:states)")
    suspend fun getOperations(
        dates: List<LocalDate>,
        states: List<OperationState>,
    ): List<OperationWithCategory>

    @Query("SELECT COUNT(*) FROM Operation WHERE categoryId IS NULL")
    fun getUncategorizedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM Operation WHERE isWaitingForAction = 1")
    fun getWaitingForActionCount(): Flow<Int>

    @Query("SELECT MIN(date) FROM Operation")
    fun getFirstOperationDate(): Flow<LocalDate?>

    @Query(
        "SELECT categoryId, SUM(amount) AS total FROM Operation " +
            "WHERE categoryId IS NOT NULL AND date BETWEEN :start AND :end GROUP BY categoryId",
    )
    fun getCategoryTotals(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<CategoryTotal>>

    // Per month from start, oldest first, with "yyyy-MM" months (dates are stored as epoch days). Expenses are those of
    // expense categories (refunds deducted) and the uncategorized negative amounts, earnings those of earning categories
    // and the uncategorized positive amounts. Internal categories are transfers, so they count in neither.
    @Query(
        "SELECT strftime('%Y-%m', Operation.date * 86400, 'unixepoch') AS month, " +
            "TOTAL(CASE WHEN Category.type = 'Expense' OR (Category.id IS NULL AND Operation.amount < 0) " +
            "THEN Operation.amount ELSE 0 END) AS expenses, " +
            "TOTAL(CASE WHEN Category.type = 'Earning' OR (Category.id IS NULL AND Operation.amount > 0) " +
            "THEN Operation.amount ELSE 0 END) AS earnings " +
            "FROM Operation LEFT JOIN Category ON Operation.categoryId = Category.id " +
            "WHERE Operation.date >= :start GROUP BY month ORDER BY month",
    )
    fun getMonthTotals(start: LocalDate): Flow<List<MonthTotal>>

    // Null when there is none
    @Query("SELECT SUM(amount) FROM Operation WHERE categoryId IS NULL AND amount < 0 AND date BETWEEN :start AND :end")
    fun getUncategorizedExpensesTotal(
        start: LocalDate,
        end: LocalDate,
    ): Flow<Double?>

    @Insert
    suspend fun insertOperation(operation: OperationEntity)

    @Insert
    suspend fun insertOperations(operations: List<OperationEntity>)

    @Update
    suspend fun updateOperation(operation: OperationEntity)

    @Delete
    suspend fun deleteOperation(operation: OperationEntity)

    @Query("DELETE FROM Operation WHERE id IN (:ids)")
    suspend fun deleteOperations(ids: Set<Long>)

    // Sorted by OperationRepository, as their order depends on the hue of their color
    @Query("SELECT * FROM Category")
    fun getCategories(): Flow<List<CategoryEntity>>

    // Sorted by OperationRepository, as their order depends on the hue of their color
    @Query("SELECT * FROM Category WHERE budget IS NOT NULL")
    fun getBudgetCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT color FROM Category WHERE id != :categoryId")
    suspend fun fetchOtherCategoryColors(categoryId: Long): List<Int>

    @Query("SELECT * FROM Category WHERE id = :id")
    suspend fun getCategory(id: Long): CategoryEntity?

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

    @Transaction
    @Query("SELECT * FROM SortingRule ORDER BY textToRecognize COLLATE NOCASE")
    fun getSortingRules(): Flow<List<SortingRuleWithCategory>>

    @Transaction
    @Query("SELECT * FROM SortingRule")
    suspend fun fetchSortingRules(): List<SortingRuleWithCategory>

    @Transaction
    @Query("SELECT * FROM SortingRule WHERE id = :id")
    suspend fun getSortingRule(id: Long): SortingRuleWithCategory?

    @Insert
    suspend fun insertSortingRule(sortingRule: SortingRuleEntity)

    @Update
    suspend fun updateSortingRule(sortingRule: SortingRuleEntity)

    @Delete
    suspend fun deleteSortingRule(sortingRule: SortingRuleEntity)
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
