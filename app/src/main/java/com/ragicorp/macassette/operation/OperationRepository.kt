package com.ragicorp.macassette.operation

import android.content.Context

class OperationRepository(
    context: Context,
) {
    private val operationDao = OperationDatabase.getInstance(context).operationDao()

    suspend fun getOperations(): List<Operation> = operationDao.getOperations()

    suspend fun insertCategory(category: Category) = operationDao.insertCategory(category)
}
