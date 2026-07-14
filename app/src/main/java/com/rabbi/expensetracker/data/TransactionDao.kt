package com.rabbi.expensetracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getInRange(start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM transactions WHERE sender = :sender AND timestamp = :timestamp AND note = :note")
    suspend fun countDuplicate(sender: String, timestamp: Long, note: String): Int

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE timestamp BETWEEN :start AND :end")
    suspend fun deleteInRange(start: Long, end: Long)
}
