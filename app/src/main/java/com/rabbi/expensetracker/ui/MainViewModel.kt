package com.rabbi.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rabbi.expensetracker.data.AppDatabase
import com.rabbi.expensetracker.data.Transaction
import com.rabbi.expensetracker.data.TransactionRepository
import com.rabbi.expensetracker.data.TxSource
import com.rabbi.expensetracker.data.TxType
import com.rabbi.expensetracker.sms.SmsInboxScanner
import com.rabbi.expensetracker.util.CsvExporter
import com.rabbi.expensetracker.util.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthCursor(val year: Int, val month: Int) { // month: 0-11
    fun rangeMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        return start to end
    }

    fun label(): String {
        val months = listOf("January","February","March","April","May","June","July","August","September","October","November","December")
        return "${months[month]} $year"
    }

    fun next() = if (month == 11) MonthCursor(year + 1, 0) else MonthCursor(year, month + 1)
    fun prev() = if (month == 0) MonthCursor(year - 1, 11) else MonthCursor(year, month - 1)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TransactionRepository(AppDatabase.getInstance(application).transactionDao())
    val prefs = Prefs(application)

    private val _cursor = MutableStateFlow(run {
        val cal = Calendar.getInstance()
        MonthCursor(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    })
    val cursor: StateFlow<MonthCursor> = _cursor

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _lastImportCount = MutableStateFlow<Int?>(null)
    val lastImportCount: StateFlow<Int?> = _lastImportCount

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthTransactions: StateFlow<List<Transaction>> = _cursor
        .flatMapLatest { c ->
            val (start, end) = c.rangeMillis()
            repo.getInRange(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = monthTransactions
        .map { list -> list.filter { it.type == TxType.INCOME }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = monthTransactions
        .map { list -> list.filter { it.type == TxType.EXPENSE }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryBreakdown: StateFlow<Map<String, Double>> = monthTransactions
        .map { list ->
            list.filter { it.type == TxType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount } }
                .toList().sortedByDescending { it.second }.toMap()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun nextMonth() { _cursor.value = _cursor.value.next() }
    fun prevMonth() { _cursor.value = _cursor.value.prev() }

    fun addManual(amount: Double, type: TxType, category: String, note: String) {
        viewModelScope.launch {
            repo.addManual(
                Transaction(
                    amount = amount, type = type, category = category, note = note,
                    sender = "Manual", timestamp = System.currentTimeMillis(), source = TxSource.MANUAL
                )
            )
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { repo.delete(transaction) }
    }

    fun updateTransaction(transaction: Transaction, category: String, note: String) {
        viewModelScope.launch {
            repo.update(transaction.copy(category = category, note = note))
        }
    }

    /** Deletes every transaction in the currently viewed month. Irreversible. */
    fun resetCurrentMonth() {
        viewModelScope.launch {
            val (start, end) = _cursor.value.rangeMillis()
            repo.resetRange(start, end)
        }
    }

    fun scanInboxHistory(months: Int = 6) {
        viewModelScope.launch {
            _isScanning.value = true
            val since = Calendar.getInstance().apply { add(Calendar.MONTH, -months) }.timeInMillis
            val added = SmsInboxScanner.scanInbox(getApplication(), repo, since)
            prefs.lastInboxScanMillis = System.currentTimeMillis()
            _lastImportCount.value = added
            _isScanning.value = false
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            CsvExporter.export(getApplication(), monthTransactions.value, _cursor.value.label().replace(" ", "_"))
        }
    }
}
