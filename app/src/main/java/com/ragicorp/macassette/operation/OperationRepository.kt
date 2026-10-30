package com.ragicorp.macassette.operation

import android.content.Context
import com.ragicorp.macassette.operation.db.OperationDatabase
import com.ragicorp.macassette.operation.db.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.YearMonth

class OperationRepository(
    context: Context,
) {
    private val operationDao = OperationDatabase.getInstance(context).operationDao()

    fun getOperations(): Flow<List<Operation>> = operationDao.getOperations().map { operations -> operations.map { it.toOperation() } }

    // Month of the first recorded operation, null while there is none
    fun getFirstOperationMonth(): Flow<YearMonth?> = operationDao.getFirstOperationDate().map { date -> date?.let(YearMonth::from) }

    suspend fun insertOperation(operation: Operation) = operationDao.insertOperation(operation.toEntity())

    // Saves the operations of a statement that weren't already imported, and returns how many were saved
    suspend fun importOperations(operations: List<Operation>): Int {
        val unmatchedSavedOperations =
            operationDao
                .getOperations(
                    dates = operations.map { it.date }.distinct(),
                    states = listOf(OperationState.AutoImported, OperationState.Merged),
                ).map { it.toOperation() }
                .toMutableList()
        // An operation already saved at the same date with the same label and amount was imported by a previous
        // statement. Each one only accounts for a single line, so identical operations on the same day are all kept.
        val newOperations =
            operations.filterNot { operation ->
                unmatchedSavedOperations
                    .firstOrNull { it.date == operation.date && it.label == operation.label && it.amount == operation.amount }
                    ?.let { unmatchedSavedOperations.remove(it) } == true
            }
        operationDao.insertOperations(newOperations.map { it.toEntity() })
        return newOperations.size
    }

    suspend fun updateOperation(operation: Operation) = operationDao.updateOperation(operation.toEntity())

    suspend fun deleteOperation(operation: Operation) = operationDao.deleteOperation(operation.toEntity())

    suspend fun deleteOperations(ids: Set<Long>) = operationDao.deleteOperations(ids)

    fun getCategories(): Flow<List<Category>> = operationDao.getCategories().map { categories -> categories.map { it.toCategory() } }

    fun getBudgetCategories(): Flow<List<Category>> =
        operationDao.getBudgetCategories().map { categories -> categories.map { it.toCategory() } }

    suspend fun insertCategory(category: Category) = operationDao.insertCategory(category.toEntity())

    suspend fun updateCategory(category: Category) = operationDao.updateCategory(category.toEntity())

    suspend fun deleteCategory(category: Category) = operationDao.deleteCategory(category.toEntity())

    fun getParsers(): Flow<List<Parser>> = operationDao.getParsers().map { parsers -> parsers.map { it.toParser() } }

    // Parsers whose three labels are all columns of a file
    fun getParsers(columns: List<String>): Flow<List<Parser>> =
        getParsers().map { parsers ->
            parsers.filter { parser -> listOf(parser.dateLabel, parser.nameLabel, parser.amountLabel).all { it in columns } }
        }

    suspend fun insertParser(parser: Parser) = operationDao.insertParser(parser.toEntity())

    suspend fun deleteParser(parser: Parser) = operationDao.deleteParser(parser.toEntity())
}
