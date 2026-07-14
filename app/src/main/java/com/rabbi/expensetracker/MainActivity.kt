package com.rabbi.expensetracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rabbi.expensetracker.ui.MainViewModel
import com.rabbi.expensetracker.ui.screens.AddTransactionScreen
import com.rabbi.expensetracker.ui.screens.DashboardScreen
import com.rabbi.expensetracker.ui.screens.SettingsScreen
import com.rabbi.expensetracker.ui.screens.TransactionsScreen
import com.rabbi.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val smsGranted = results[Manifest.permission.READ_SMS] == true
        if (smsGranted) viewModel.scanInboxHistory(6)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = mutableListOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissions.launch(permissions.toTypedArray())

        setContent {
            val darkOverride = viewModel.prefs.darkModeOverride
            val dynamicColor = viewModel.prefs.dynamicColorEnabled

            ExpenseTrackerTheme(
                darkTheme = darkOverride ?: androidx.compose.foundation.isSystemInDarkTheme(),
                dynamicColor = dynamicColor
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppScaffold(viewModel)
                }
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val navItems = listOf(
    NavItem("dashboard", "Home", Icons.Filled.Home),
    NavItem("transactions", "History", Icons.Filled.List),
    NavItem("add", "Add", Icons.Filled.Add),
    NavItem("settings", "Settings", Icons.Filled.Settings)
)

@Composable
private fun AppScaffold(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") {
                DashboardScreen(viewModel) {
                    navController.navigate("transactions")
                }
            }
            composable("transactions") { TransactionsScreen(viewModel) }
            composable("add") {
                AddTransactionScreen(viewModel) {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            }
            composable("settings") { SettingsScreen(viewModel) }
        }
    }
}
