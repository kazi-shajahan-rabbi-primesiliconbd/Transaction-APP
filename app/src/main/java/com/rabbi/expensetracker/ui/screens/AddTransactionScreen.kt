package com.rabbi.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rabbi.expensetracker.data.DEFAULT_CATEGORIES
import com.rabbi.expensetracker.data.TxType
import com.rabbi.expensetracker.ui.MainViewModel
import com.rabbi.expensetracker.ui.theme.ExpenseRed
import com.rabbi.expensetracker.ui.theme.IncomeGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(viewModel: MainViewModel, onSaved: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TxType.EXPENSE) }
    var category by remember { mutableStateOf(DEFAULT_CATEGORIES.first()) }
    var note by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull()
    val isValid = amount != null && amount > 0.0

    Column(
        Modifier.fillMaxSize().padding(20.dp)
    ) {
        Text("Add transaction", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        SingleChoiceSegment(
            selected = type,
            onSelect = { type = it }
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) amountText = input },
            label = { Text("Amount (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        Text("Category", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DEFAULT_CATEGORIES) { cat ->
                FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
            }
        }
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.addManual(amount!!, type, category, note)
                onSaved()
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Save transaction", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SingleChoiceSegment(selected: TxType, onSelect: (TxType) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SegmentButton(
            label = "Expense",
            isSelected = selected == TxType.EXPENSE,
            color = ExpenseRed,
            modifier = Modifier.weight(1f)
        ) { onSelect(TxType.EXPENSE) }
        SegmentButton(
            label = "Income",
            isSelected = selected == TxType.INCOME,
            color = IncomeGreen,
            modifier = Modifier.weight(1f)
        ) { onSelect(TxType.INCOME) }
    }
}

@Composable
private fun SegmentButton(
    label: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent,
            contentColor = color
        )
    ) {
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
