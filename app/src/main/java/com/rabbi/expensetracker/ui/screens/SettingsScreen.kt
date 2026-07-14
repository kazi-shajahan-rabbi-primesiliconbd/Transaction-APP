package com.rabbi.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabbi.expensetracker.ui.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val cursor by viewModel.cursor.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val lastImport by viewModel.lastImportCount.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }
    var darkOverride by remember { mutableStateOf(viewModel.prefs.darkModeOverride) }
    var dynamicColor by remember { mutableStateOf(viewModel.prefs.dynamicColorEnabled) }
    var smsImport by remember { mutableStateOf(viewModel.prefs.smsImportEnabled) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        SettingsSection(title = "Appearance") {
            SettingsSwitchRow(
                label = "Use device wallpaper colors",
                checked = dynamicColor
            ) {
                dynamicColor = it
                viewModel.prefs.dynamicColorEnabled = it
            }
            SettingsSwitchRow(
                label = "Force dark theme",
                checked = darkOverride == true
            ) { checked ->
                darkOverride = if (checked) true else null
                viewModel.prefs.darkModeOverride = darkOverride
            }
        }

        Spacer(Modifier.height(24.dp))

        SettingsSection(title = "SMS import") {
            SettingsSwitchRow(
                label = "Auto-detect new transactions from SMS",
                checked = smsImport
            ) {
                smsImport = it
                viewModel.prefs.smsImportEnabled = it
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.scanInboxHistory(6) },
                enabled = !isScanning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isScanning) "Scanning inbox..." else "Re-scan last 6 months of SMS")
            }
            lastImport?.let {
                Spacer(Modifier.height(6.dp))
                Text("Imported $it new transaction(s) on last scan.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        SettingsSection(title = "Data") {
            OutlinedButton(onClick = { viewModel.exportCsv() }, modifier = Modifier.fillMaxWidth()) {
                Text("Export ${cursor.label()} as CSV")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset ${cursor.label()}", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset ${cursor.label()}?") },
            text = { Text("This permanently deletes every income and expense recorded for ${cursor.label()}. Export a CSV first if you want a copy. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetCurrentMonth()
                    showResetDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Column(content = content)
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
