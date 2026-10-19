package com.ragicorp.macassette.operation

import android.content.Context
import com.ragicorp.macassette.operation.db.OperationDatabase
import com.ragicorp.macassette.operation.db.toEntity

class OperationRepository(
    context: Context,
) {
    private val operationDao = OperationDatabase.getInstance(context).operationDao()

    suspend fun getOperations(): List<Operation> = operationDao.getOperations()

    suspend fun insertCategory(category: Category) = operationDao.insertCategory(category.toEntity())
}
