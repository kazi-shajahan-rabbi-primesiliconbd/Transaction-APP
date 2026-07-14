package com.rabbi.expensetracker.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabbi.expensetracker.data.Transaction
import com.rabbi.expensetracker.ui.MainViewModel
import com.rabbi.expensetracker.ui.components.EditTransactionDialog
import com.rabbi.expensetracker.ui.components.TransactionRow
import com.rabbi.expensetracker.ui.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: MainViewModel) {
    val transactions by viewModel.monthTransactions.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val categories = remember(transactions) { transactions.map { it.category }.distinct().sorted() }

    val filtered = remember(transactions, query, categoryFilter) {
        transactions.filter { tx ->
            (categoryFilter == null || tx.category == categoryFilter) &&
                (query.isBlank() || tx.category.contains(query, true) || tx.note.contains(query, true))
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search transactions") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        if (categories.isNotEmpty()) {
            LazyRowFilterChips(categories, categoryFilter) { categoryFilter = it }
            Spacer(Modifier.height(8.dp))
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions match.")
            }
        } else {
            LazyColumn {
                items(filtered, key = { it.id }) { tx ->
                    SwipeToDeleteRow(
                        transaction = tx,
                        onDelete = { viewModel.delete(tx) },
                        onClick = { editingTransaction = tx }
                    )
                    HorizontalDivider()
                }
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

@Composable
private fun LazyRowFilterChips(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        }
        items(categories) { cat ->
            FilterChip(selected = selected == cat, onClick = { onSelect(if (selected == cat) null else cat) }, label = { Text(cat) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRow(transaction: Transaction, onDelete: () -> Unit, onClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(ExpenseRed.copy(alpha = 0.15f)).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ExpenseRed)
            }
        }
    ) {
        Surface { TransactionRow(transaction, onClick = onClick) }
    }
}
