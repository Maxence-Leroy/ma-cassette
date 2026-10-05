package com.ragicorp.macassette.operation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val budget: Double? = null,
    val isEarning: Boolean = false,
)
