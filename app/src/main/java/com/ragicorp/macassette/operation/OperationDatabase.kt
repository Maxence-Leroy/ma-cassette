package com.ragicorp.macassette.operation

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Operation::class], version = 1)
abstract class OperationDatabase : RoomDatabase() {
    abstract fun operationDao(): OperationDao
}
