package com.rabbi.expensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rabbi.expensetracker.data.DEFAULT_CATEGORIES
import com.rabbi.expensetracker.data.Transaction

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (category: String, note: String) -> Unit
) {
    var category by remember { mutableStateOf(transaction.category) }
    var note by remember { mutableStateOf(transaction.note) }

    // Make sure the current category shows as a selectable option even if
    // it isn't one of the defaults (e.g. it came from SMS auto-categorization).
    val categoryOptions = remember(category) {
        if (DEFAULT_CATEGORIES.contains(category)) DEFAULT_CATEGORIES
        else listOf(category) + DEFAULT_CATEGORIES
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit transaction") },
        text = {
            Column {
                Text("Title / category", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryOptions) { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Custom title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Description / note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(category.ifBlank { transaction.category }, note) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
