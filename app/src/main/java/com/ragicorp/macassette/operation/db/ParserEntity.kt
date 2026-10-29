package com.ragicorp.macassette.operation.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ragicorp.macassette.operation.Parser

@Entity(tableName = "Parser")
data class ParserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateLabel: String,
    val nameLabel: String,
    val amountLabel: String,
) {
    fun toParser() =
        Parser(
            id = id,
            name = name,
            dateLabel = dateLabel,
            nameLabel = nameLabel,
            amountLabel = amountLabel,
        )
}

fun Parser.toEntity() =
    ParserEntity(
        id = id,
        name = name,
        dateLabel = dateLabel,
        nameLabel = nameLabel,
        amountLabel = amountLabel,
    )
