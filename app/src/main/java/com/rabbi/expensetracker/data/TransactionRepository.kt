package com.rabbi.expensetracker.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    fun getAll(): Flow<List<Transaction>> = dao.getAll()

    fun getInRange(start: Long, end: Long): Flow<List<Transaction>> = dao.getInRange(start, end)

    suspend fun addManual(transaction: Transaction) {
        dao.insert(transaction)
    }

    // Prevents the same SMS being inserted twice (e.g. if both the initial
    // inbox scan and the live receiver see it).
    suspend fun addFromSms(transaction: Transaction): Boolean {
        val existing = dao.countDuplicate(transaction.sender, transaction.timestamp, transaction.note)
        if (existing > 0) return false
        dao.insert(transaction)
        return true
    }

    suspend fun update(transaction: Transaction) = dao.update(transaction)

    suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    suspend fun resetRange(start: Long, end: Long) = dao.deleteInRange(start, end)
}
