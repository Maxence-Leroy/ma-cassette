package com.ragicorp.macassette.operation.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationState
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("categoryId")],
    tableName = "Operation",
)
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long? = null,
    val date: LocalDate,
    val amount: Double,
    val label: String,
    val notes: String,
    val recurrentPeriod: Period? = null,
    val isWaitingForAction: Boolean,
    val state: OperationState,
)

fun Operation.toEntity() =
    OperationEntity(
        id = id,
        categoryId = category?.id,
        date = date,
        amount = amount,
        label = label,
        notes = notes,
        recurrentPeriod = recurrentPeriod,
        isWaitingForAction = isWaitingForAction,
        state = state,
    )
