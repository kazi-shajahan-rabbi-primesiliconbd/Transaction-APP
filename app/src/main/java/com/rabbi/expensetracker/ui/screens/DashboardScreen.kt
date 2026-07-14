package com.rabbi.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabbi.expensetracker.data.Transaction
import com.rabbi.expensetracker.ui.MainViewModel
import com.rabbi.expensetracker.ui.components.CategoryPieChart
import com.rabbi.expensetracker.ui.components.EditTransactionDialog
import com.rabbi.expensetracker.ui.components.SummaryCard
import com.rabbi.expensetracker.ui.components.TransactionRow
import com.rabbi.expensetracker.ui.components.formatTaka
import com.rabbi.expensetracker.ui.theme.ExpenseRed
import com.rabbi.expensetracker.ui.theme.IncomeGreen

@Composable
fun DashboardScreen(viewModel: MainViewModel, onSeeAll: () -> Unit) {
    val cursor by viewModel.cursor.collectAsStateWithLifecycle()
    val income by viewModel.totalIncome.collectAsStateWithLifecycle()
    val expense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val breakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val transactions by viewModel.monthTransactions.collectAsStateWithLifecycle()
    val balance = income - expense
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.prevMonth() }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(cursor.label(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }
            Spacer(Modifier.height(16.dp))

            Card(shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(20.dp)) {
                    Text("Balance", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatTaka(balance),
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (balance >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(Modifier.weight(1f), "Income", income, IncomeGreen)
                SummaryCard(Modifier.weight(1f), "Expenses", expense, ExpenseRed)
            }
            Spacer(Modifier.height(24.dp))

            Text("Spending by category", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            CategoryPieChart(breakdown)
            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recent transactions", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onSeeAll) { Text("See all") }
            }
            Spacer(Modifier.height(4.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp)) {
                    Text("No transactions yet this month. Add one, or let SMS scanning find them.")
                }
            }
        } else {
            items(transactions.take(6), key = { it.id }) { tx ->
                TransactionRow(tx, onClick = { editingTransaction = tx })
                HorizontalDivider()
            }
        }
    }

    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { editingTransaction = null },
            onSave = { category, note ->
                viewModel.updateTransaction(tx, category, note)
                editingTransaction = null
            }
        )
    }
}
