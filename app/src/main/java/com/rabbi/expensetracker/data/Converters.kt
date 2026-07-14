package com.rabbi.expensetracker.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTxType(type: TxType): String = type.name

    @TypeConverter
    fun toTxType(value: String): TxType = TxType.valueOf(value)

    @TypeConverter
    fun fromTxSource(source: TxSource): String = source.name

    @TypeConverter
    fun toTxSource(value: String): TxSource = TxSource.valueOf(value)
}
