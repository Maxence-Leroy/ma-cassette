package com.ragicorp.macassette.operation.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ragicorp.macassette.operation.SortingRule

// A rule can't exist without its category, so it is deleted along with it
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryToApplyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryToApplyId")],
    tableName = "SortingRule",
)
data class SortingRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val textToRecognize: String,
    val categoryToApplyId: Long,
)

data class SortingRuleWithCategory(
    @Embedded val entity: SortingRuleEntity,
    @Relation(parentColumn = "categoryToApplyId", entityColumn = "id")
    val categoryToApply: CategoryEntity,
) {
    fun toSortingRule() =
        SortingRule(
            id = entity.id,
            textToRecognize = entity.textToRecognize,
            categoryToApply = categoryToApply.toCategory(),
        )
}

fun SortingRule.toEntity() =
    SortingRuleEntity(
        id = id,
        textToRecognize = textToRecognize,
        categoryToApplyId = categoryToApply.id,
    )
