package com.ragicorp.macassette.operation

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

class Converters {
    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromPeriodString(value: String?): Period? = value?.let(Period::parse)

    @TypeConverter
    fun periodToString(period: Period?): String? = period?.toString()
}
