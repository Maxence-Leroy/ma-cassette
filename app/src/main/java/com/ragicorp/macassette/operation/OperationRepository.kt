package com.ragicorp.macassette.operation

import android.content.Context
import com.ragicorp.macassette.operation.db.OperationDatabase
import com.ragicorp.macassette.operation.db.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OperationRepository(
    context: Context,
) {
    private val operationDao = OperationDatabase.getInstance(context).operationDao()

    suspend fun getOperations(): List<Operation> = operationDao.getOperations()

    suspend fun insertOperation(operation: Operation) = operationDao.insertOperation(operation.toEntity())

    fun getCategories(): Flow<List<Category>> = operationDao.getCategories().map { categories -> categories.map { it.toCategory() } }

    suspend fun insertCategory(category: Category) = operationDao.insertCategory(category.toEntity())

    suspend fun updateCategory(category: Category) = operationDao.updateCategory(category.toEntity())

    suspend fun deleteCategory(category: Category) = operationDao.deleteCategory(category.toEntity())
}
