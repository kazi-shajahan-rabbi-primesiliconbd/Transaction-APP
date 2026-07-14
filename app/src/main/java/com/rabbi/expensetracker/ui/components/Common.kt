package com.rabbi.expensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rabbi.expensetracker.data.Transaction
import com.rabbi.expensetracker.data.TxType
import com.rabbi.expensetracker.ui.theme.ExpenseRed
import com.rabbi.expensetracker.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTaka(amount: Double): String = "৳${"%,.2f".format(amount)}"

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                formatTaka(amount),
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, onClick: () -> Unit = {}) {
    val isExpense = transaction.type == TxType.EXPENSE
    val sdf = remember { SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()) }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(transaction.category, fontWeight = FontWeight.Medium) },
        supportingContent = {
            val dateText = sdf.format(Date(transaction.timestamp))
            val displayNote = transaction.note.takeIf { it.isNotBlank() && it != transaction.category }
            Text(if (displayNote != null) "$dateText · ${displayNote.take(60)}" else dateText)
        },
        leadingContent = {
            Icon(
                imageVector = if (isExpense) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = if (isExpense) ExpenseRed else IncomeGreen
            )
        },
        trailingContent = {
            Text(
                (if (isExpense) "-" else "+") + formatTaka(transaction.amount),
                color = if (isExpense) ExpenseRed else IncomeGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}
