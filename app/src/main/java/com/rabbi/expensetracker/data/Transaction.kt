package com.rabbi.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TxType { INCOME, EXPENSE }
enum class TxSource { SMS, MANUAL }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TxType,
    val category: String,
    val note: String,
    val sender: String,
    val timestamp: Long,
    val source: TxSource
)

// Default categories offered to the user, editable in Add screen.
val DEFAULT_CATEGORIES = listOf(
    "Cash Out", "Send Money", "Mobile Recharge", "Bill Payment",
    "Groceries", "Food & Dining", "Transport", "Shopping",
    "Salary", "Refund", "Transfer", "Other"
)
