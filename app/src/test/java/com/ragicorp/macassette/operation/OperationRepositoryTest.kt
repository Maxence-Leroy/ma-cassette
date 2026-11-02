package com.ragicorp.macassette.operation

import android.content.Context
import com.ragicorp.macassette.operation.db.CategoryEntity
import com.ragicorp.macassette.operation.db.CategoryTotal
import com.ragicorp.macassette.operation.db.OperationDao
import com.ragicorp.macassette.operation.db.OperationDatabase
import com.ragicorp.macassette.operation.db.OperationEntity
import com.ragicorp.macassette.operation.db.OperationWithCategory
import com.ragicorp.macassette.operation.db.SortingRuleWithCategory
import com.ragicorp.macassette.operation.db.toEntity
import com.ragicorp.macassette.utils.toUnusedShade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class OperationRepositoryTest {
    private val operationDao = mockk<OperationDao>()
    private val insertedOperations = slot<List<OperationEntity>>()
    private lateinit var repository: OperationRepository

    private val groceries = Category(id = 1, name = "Groceries", color = 0, type = CategoryType.Expense)
    private val restaurants = Category(id = 2, name = "Restaurants", color = 0, type = CategoryType.Expense)
    private val groceriesColor = 0xFF43A047.toInt()
    private val restaurantsColor = 0xFFFB8C00.toInt()
    private val firstDay = LocalDate.of(2026, 10, 1)
    private val secondDay = LocalDate.of(2026, 10, 2)

    @Before
    fun setUp() {
        val database = mockk<OperationDatabase>()
        every { database.operationDao() } returns operationDao
        // The repository gets its DAO from the database singleton, so the singleton is mocked to hand out the mocked DAO
        mockkObject(OperationDatabase)
        every { OperationDatabase.getInstance(any()) } returns database

        coEvery { operationDao.getOperations(dates = any(), states = any()) } returns emptyList()
        coEvery { operationDao.fetchSortingRules() } returns emptyList()
        coEvery { operationDao.insertOperations(capture(insertedOperations)) } returns Unit

        repository = OperationRepository(mockk<Context>())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `new operations are all saved`() =
        runTest {
            val operations = listOf(operation(label = "Bakery"), operation(label = "Cinema", date = secondDay))

            val result = repository.importOperations(operations)

            assertEquals(operations.map { it.toEntity() }, insertedOperations.captured)
            assertEquals(ImportResult(importedCount = 2, ambiguousOperations = emptyList()), result)
        }

    @Test
    fun `saved operations are looked up on the statement dates among imported and merged ones`() =
        runTest {
            repository.importOperations(
                listOf(operation(label = "Bakery"), operation(label = "Cinema"), operation(label = "Pharmacy", date = secondDay)),
            )

            coVerify {
                operationDao.getOperations(
                    dates = listOf(firstDay, secondDay),
                    states = listOf(OperationState.AutoImported, OperationState.Merged),
                )
            }
        }

    @Test
    fun `operations already saved by a previous import are skipped`() =
        runTest {
            savedOperations(operation(id = 10, label = "Bakery"))

            val result = repository.importOperations(listOf(operation(label = "Bakery"), operation(label = "Cinema")))

            assertEquals(listOf(operation(label = "Cinema").toEntity()), insertedOperations.captured)
            assertEquals(1, result.importedCount)
        }

    @Test
    fun `an operation is only a duplicate with the same date, label and amount`() =
        runTest {
            savedOperations(operation(id = 10, label = "Bakery", amount = -5.0))
            val operations =
                listOf(
                    operation(label = "Bakery", amount = -5.0, date = secondDay),
                    operation(label = "Cinema", amount = -5.0),
                    operation(label = "Bakery", amount = -6.0),
                )

            val result = repository.importOperations(operations)

            assertEquals(operations.map { it.toEntity() }, insertedOperations.captured)
            assertEquals(3, result.importedCount)
        }

    @Test
    fun `each saved operation only accounts for a single identical operation`() =
        runTest {
            savedOperations(operation(id = 10, label = "Bakery"))

            val result = repository.importOperations(listOf(operation(label = "Bakery"), operation(label = "Bakery")))

            assertEquals(listOf(operation(label = "Bakery").toEntity()), insertedOperations.captured)
            assertEquals(1, result.importedCount)
        }

    @Test
    fun `operations matched by a sorting rule get its category, ignoring case`() =
        runTest {
            sortingRules(rule(text = "carrefour", category = groceries))

            val result = repository.importOperations(listOf(operation(label = "CB CARREFOUR CITY"), operation(label = "Cinema")))

            assertEquals(
                listOf(operation(label = "CB CARREFOUR CITY", category = groceries).toEntity(), operation(label = "Cinema").toEntity()),
                insertedOperations.captured,
            )
            assertEquals(ImportResult(importedCount = 2, ambiguousOperations = emptyList()), result)
        }

    @Test
    fun `operations matched by several rules applying the same category are categorized`() =
        runTest {
            sortingRules(rule(id = 1, text = "carrefour", category = groceries), rule(id = 2, text = "city", category = groceries))

            val result = repository.importOperations(listOf(operation(label = "Carrefour City")))

            assertEquals(listOf(operation(label = "Carrefour City", category = groceries).toEntity()), insertedOperations.captured)
            assertEquals(ImportResult(importedCount = 1, ambiguousOperations = emptyList()), result)
        }

    @Test
    fun `operations matched by rules applying different categories are returned unsaved`() =
        runTest {
            sortingRules(rule(id = 1, text = "carrefour", category = groceries), rule(id = 2, text = "city", category = restaurants))

            val result = repository.importOperations(listOf(operation(label = "Carrefour City"), operation(label = "Cinema")))

            assertEquals(listOf(operation(label = "Cinema").toEntity()), insertedOperations.captured)
            assertEquals(
                ImportResult(
                    importedCount = 1,
                    ambiguousOperations =
                        listOf(AmbiguousOperation(operation(label = "Carrefour City"), listOf(groceries, restaurants))),
                ),
                result,
            )
        }

    @Test
    fun `operations already saved are neither saved nor returned as ambiguous`() =
        runTest {
            savedOperations(operation(id = 10, label = "Carrefour City"))
            sortingRules(rule(id = 1, text = "carrefour", category = groceries), rule(id = 2, text = "city", category = restaurants))

            val result = repository.importOperations(listOf(operation(label = "Carrefour City")))

            assertEquals(emptyList<OperationEntity>(), insertedOperations.captured)
            assertEquals(ImportResult(importedCount = 0, ambiguousOperations = emptyList()), result)
        }

    @Test
    fun `a new category with an unused color keeps it`() =
        runTest {
            val insertedCategory = slot<CategoryEntity>()
            coEvery { operationDao.fetchOtherCategoryColors(0) } returns listOf(restaurantsColor)
            coEvery { operationDao.insertCategory(capture(insertedCategory)) } returns Unit

            repository.insertCategory(groceries.copy(id = 0, color = groceriesColor))

            assertEquals(groceriesColor, insertedCategory.captured.color)
        }

    @Test
    fun `a new category with a color already given gets another one`() =
        runTest {
            val insertedCategory = slot<CategoryEntity>()
            coEvery { operationDao.fetchOtherCategoryColors(0) } returns listOf(groceriesColor, restaurantsColor)
            coEvery { operationDao.insertCategory(capture(insertedCategory)) } returns Unit

            repository.insertCategory(groceries.copy(id = 0, color = groceriesColor))

            assertNotEquals(groceriesColor, insertedCategory.captured.color)
            assertNotEquals(restaurantsColor, insertedCategory.captured.color)
        }

    @Test
    fun `an edited category keeps a color no other category has`() =
        runTest {
            val updatedCategory = slot<CategoryEntity>()
            coEvery { operationDao.fetchOtherCategoryColors(groceries.id) } returns listOf(restaurantsColor)
            coEvery { operationDao.updateCategory(capture(updatedCategory)) } returns Unit

            repository.updateCategory(groceries.copy(color = groceriesColor))

            assertEquals(groceriesColor, updatedCategory.captured.color)
        }

    @Test
    fun `an edited category given the color of another one gets another one`() =
        runTest {
            val updatedCategory = slot<CategoryEntity>()
            coEvery { operationDao.fetchOtherCategoryColors(groceries.id) } returns listOf(restaurantsColor)
            coEvery { operationDao.updateCategory(capture(updatedCategory)) } returns Unit

            repository.updateCategory(groceries.copy(color = restaurantsColor))

            assertEquals(groceries.id, updatedCategory.captured.id)
            assertNotEquals(restaurantsColor, updatedCategory.captured.color)
        }

    @Test
    fun `categories are sorted by type, then hue, then name`() =
        runTest {
            val yellow = 0xFFFDD835.toInt()
            val darkerYellow = yellow.toUnusedShade(setOf(yellow))
            val categories =
                listOf(
                    Category(id = 1, name = "Salary", color = 0xFFE53935.toInt(), type = CategoryType.Earning),
                    Category(id = 2, name = "Leisure", color = yellow, type = CategoryType.Expense),
                    Category(id = 3, name = "Groceries", color = restaurantsColor, type = CategoryType.Expense),
                    Category(id = 4, name = "Books", color = darkerYellow, type = CategoryType.Expense),
                    Category(id = 5, name = "Health", color = 0xFFE53935.toInt(), type = CategoryType.Expense),
                )
            every { operationDao.getCategories() } returns flowOf(categories.map { it.toEntity() })

            val sortedNames = repository.getCategories().first().map { it.name }

            assertEquals(listOf("Health", "Groceries", "Books", "Leisure", "Salary"), sortedNames)
        }

    @Test
    fun `expenses are the spending of each expense category over the month, sorted like categories`() =
        runTest {
            val month = YearMonth.of(2026, 10)
            val groceries = groceries.copy(color = groceriesColor)
            val health = Category(id = 5, name = "Health", color = 0xFFE53935.toInt(), type = CategoryType.Expense)
            val salary = Category(id = 6, name = "Salary", color = 0xFF1E88E5.toInt(), type = CategoryType.Earning)
            val unused = Category(id = 7, name = "Unused", color = 0xFF5E35B1.toInt(), type = CategoryType.Expense)
            val refunded = Category(id = 8, name = "Refunded", color = 0xFF00897B.toInt(), type = CategoryType.Expense)
            every { operationDao.getCategories() } returns
                flowOf(listOf(groceries, salary, unused, refunded, health).map { it.toEntity() })
            every { operationDao.getCategoryTotals(start = month.atDay(1), end = month.atEndOfMonth()) } returns
                flowOf(
                    listOf(
                        CategoryTotal(categoryId = groceries.id, total = -120.5),
                        CategoryTotal(categoryId = salary.id, total = 2000.0),
                        CategoryTotal(categoryId = refunded.id, total = 15.0),
                        CategoryTotal(categoryId = health.id, total = -30.0),
                    ),
                )

            every { operationDao.getUncategorizedExpensesTotal(start = month.atDay(1), end = month.atEndOfMonth()) } returns
                flowOf(null)

            val expenses = repository.getExpenses(month).first()

            assertEquals(
                listOf(CategoryExpense(category = health, amount = 30.0), CategoryExpense(category = groceries, amount = 120.5)),
                expenses,
            )
        }

    @Test
    fun `uncategorized expenses come after the categories`() =
        runTest {
            val month = YearMonth.of(2026, 10)
            val groceries = groceries.copy(color = groceriesColor)
            every { operationDao.getCategories() } returns flowOf(listOf(groceries.toEntity()))
            every { operationDao.getCategoryTotals(start = any(), end = any()) } returns
                flowOf(listOf(CategoryTotal(categoryId = groceries.id, total = -40.0)))
            every { operationDao.getUncategorizedExpensesTotal(start = month.atDay(1), end = month.atEndOfMonth()) } returns
                flowOf(-25.0)

            val expenses = repository.getExpenses(month).first()

            assertEquals(
                listOf(CategoryExpense(category = groceries, amount = 40.0), CategoryExpense(category = null, amount = 25.0)),
                expenses,
            )
        }

    private fun savedOperations(vararg operations: Operation) {
        coEvery { operationDao.getOperations(dates = any(), states = any()) } returns
            operations.map { OperationWithCategory(entity = it.toEntity(), category = it.category?.toEntity()) }
    }

    private fun sortingRules(vararg rules: SortingRule) {
        coEvery { operationDao.fetchSortingRules() } returns
            rules.map { SortingRuleWithCategory(entity = it.toEntity(), categoryToApply = it.categoryToApply.toEntity()) }
    }

    private fun operation(
        id: Long = 0,
        label: String,
        amount: Double = -10.0,
        date: LocalDate = firstDay,
        category: Category? = null,
    ) = Operation(
        id = id,
        category = category,
        date = date,
        amount = amount,
        label = label,
        notes = "",
        isWaitingForAction = false,
        state = OperationState.AutoImported,
    )

    private fun rule(
        id: Long = 0,
        text: String,
        category: Category,
    ) = SortingRule(id = id, textToRecognize = text, categoryToApply = category)
}
