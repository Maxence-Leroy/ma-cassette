package com.ragicorp.macassette.operation

import android.content.Context
import com.ragicorp.macassette.operation.db.OperationDatabase
import com.ragicorp.macassette.operation.db.toEntity
import com.ragicorp.macassette.utils.toHsv
import com.ragicorp.macassette.utils.toUnusedShade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import kotlin.math.roundToInt

// By type, then hue so shades of a color stay together, then name. A shade's hue can be a fraction of a degree off its
// color's, from rounding its RGB channels, hence the rounding.
private val CATEGORY_ORDER =
    compareBy<Category>(
        { it.type.ordinal },
        {
            it.color
                .toHsv()
                .hue
                .roundToInt()
        },
    ).thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }

class OperationRepository(
    context: Context,
) {
    private val operationDao = OperationDatabase.getInstance(context).operationDao()

    fun getOperations(filter: OperationFilter): Flow<List<Operation>> =
        operationDao
            .getOperations(
                isCategoryFiltered = filter.categoryIds.isNotEmpty(),
                categoryIds = filter.categoryIds.toList(),
                start = filter.start,
                end = filter.end,
                isUncategorizedOnly = filter.isUncategorizedOnly,
                isWaitingForActionOnly = filter.isWaitingForActionOnly,
            ).map { operations -> operations.map { it.toOperation() } }

    fun getUncategorizedCount(): Flow<Int> = operationDao.getUncategorizedCount()

    fun getWaitingForActionCount(): Flow<Int> = operationDao.getWaitingForActionCount()

    fun getFirstOperationDate(): Flow<LocalDate?> = operationDao.getFirstOperationDate()

    // Month of the first recorded operation, null while there is none
    fun getFirstOperationMonth(): Flow<YearMonth?> = operationDao.getFirstOperationDate().map { date -> date?.let(YearMonth::from) }

    suspend fun insertOperation(operation: Operation) = operationDao.insertOperation(operation.toEntity())

    // Saves the operations of a statement that weren't already imported, categorized by the sorting rules matching their
    // label. Those matched by rules applying different categories are returned unsaved instead, for the category to be picked.
    suspend fun importOperations(operations: List<Operation>): ImportResult {
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
        val sortingRules = operationDao.fetchSortingRules().map { it.toSortingRule() }
        val (ambiguousOperations, sortedOperations) =
            newOperations
                .map { it to findCategories(it.label, sortingRules) }
                .partition { (_, categories) -> categories.size > 1 }
        operationDao.insertOperations(
            sortedOperations.map { (operation, categories) -> operation.copy(category = categories.singleOrNull()).toEntity() },
        )
        return ImportResult(
            importedCount = sortedOperations.size,
            ambiguousOperations = ambiguousOperations.map { (operation, categories) -> AmbiguousOperation(operation, categories) },
        )
    }

    suspend fun insertOperations(operations: List<Operation>) = operationDao.insertOperations(operations.map { it.toEntity() })

    // The categories of the rules whose text is in the label, ignoring case
    private fun findCategories(
        label: String,
        sortingRules: List<SortingRule>,
    ): List<Category> =
        sortingRules
            .filter { label.lowercase().contains(it.textToRecognize.lowercase()) }
            .map { it.categoryToApply }
            .distinctBy { it.id }

    suspend fun updateOperation(operation: Operation) = operationDao.updateOperation(operation.toEntity())

    suspend fun deleteOperation(operation: Operation) = operationDao.deleteOperation(operation.toEntity())

    suspend fun deleteOperations(ids: Set<Long>) = operationDao.deleteOperations(ids)

    fun getCategories(): Flow<List<Category>> =
        operationDao.getCategories().map { categories -> categories.map { it.toCategory() }.sortedWith(CATEGORY_ORDER) }

    // Expenses are negative and refunds positive, so a category's spending is the opposite of its total. Expense
    // categories with nothing spent (or more refunded than spent) over the month are left out. Uncategorized operations
    // come last, only counting the expenses since they can also be earnings.
    fun getExpenses(month: YearMonth): Flow<List<CategoryExpense>> {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return combine(
            operationDao.getCategories(),
            operationDao.getCategoryTotals(start = start, end = end),
            operationDao.getUncategorizedExpensesTotal(start = start, end = end),
        ) { categories, totals, uncategorizedTotal ->
            val totalByCategoryId = totals.associate { it.categoryId to it.total }
            val categoryExpenses =
                categories
                    .map { it.toCategory() }
                    .filter { it.type == CategoryType.Expense }
                    .sortedWith(CATEGORY_ORDER)
                    .mapNotNull { category ->
                        val amount = -(totalByCategoryId[category.id] ?: 0.0)
                        if (amount > 0) CategoryExpense(category = category, amount = amount) else null
                    }
            val uncategorizedExpense = uncategorizedTotal?.let { CategoryExpense(category = null, amount = -it) }
            categoryExpenses + listOfNotNull(uncategorizedExpense)
        }
    }

    // Expenses are negative and refunds positive, so adding the month's operations to the budget gives what is left
    fun getBudgets(month: YearMonth): Flow<List<Budget>> =
        combine(
            operationDao.getBudgetCategories(),
            operationDao.getCategoryTotals(start = month.atDay(1), end = month.atEndOfMonth()),
        ) { categories, totals ->
            val totalByCategoryId = totals.associate { it.categoryId to it.total }
            categories.map { it.toCategory() }.sortedWith(CATEGORY_ORDER).map { category ->
                Budget(category = category, remaining = (category.budget ?: 0.0) + (totalByCategoryId[category.id] ?: 0.0))
            }
        }

    suspend fun getCategory(id: Long): Category? = operationDao.getCategory(id)?.toCategory()

    suspend fun insertCategory(category: Category) = operationDao.insertCategory(category.withUnusedColor().toEntity())

    suspend fun updateCategory(category: Category) = operationDao.updateCategory(category.withUnusedColor().toEntity())

    // Each category keeps a color of its own: one already given to another category is shifted to a free shade of it.
    // A new category's id is 0, which no saved category has.
    private suspend fun Category.withUnusedColor(): Category =
        copy(color = color.toUnusedShade(operationDao.fetchOtherCategoryColors(id).toSet()))

    suspend fun deleteCategory(category: Category) = operationDao.deleteCategory(category.toEntity())

    fun getParsers(): Flow<List<Parser>> = operationDao.getParsers().map { parsers -> parsers.map { it.toParser() } }

    // Parsers whose three labels are all columns of a file
    fun getParsers(columns: List<String>): Flow<List<Parser>> =
        getParsers().map { parsers ->
            parsers.filter { parser -> listOf(parser.dateLabel, parser.nameLabel, parser.amountLabel).all { it in columns } }
        }

    suspend fun insertParser(parser: Parser) = operationDao.insertParser(parser.toEntity())

    suspend fun deleteParser(parser: Parser) = operationDao.deleteParser(parser.toEntity())

    fun getSortingRules(): Flow<List<SortingRule>> = operationDao.getSortingRules().map { rules -> rules.map { it.toSortingRule() } }

    suspend fun getSortingRule(id: Long): SortingRule? = operationDao.getSortingRule(id)?.toSortingRule()

    suspend fun insertSortingRule(sortingRule: SortingRule) = operationDao.insertSortingRule(sortingRule.toEntity())

    suspend fun updateSortingRule(sortingRule: SortingRule) = operationDao.updateSortingRule(sortingRule.toEntity())

    suspend fun deleteSortingRule(sortingRule: SortingRule) = operationDao.deleteSortingRule(sortingRule.toEntity())
}
