package com.ragicorp.macassette.operation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [OperationEntity::class, Category::class],
    version = 1,
)
abstract class OperationDatabase : RoomDatabase() {
    abstract fun operationDao(): OperationDao

    companion object {
        private const val DATABASE_NAME = "operation-database"

        @Volatile
        private var instance: OperationDatabase? = null

        fun getInstance(context: Context): OperationDatabase =
            instance ?: synchronized(this) {
                instance ?: Room
                    .databaseBuilder(context.applicationContext, OperationDatabase::class.java, DATABASE_NAME)
                    .build()
                    .also { instance = it }
            }
    }
}
