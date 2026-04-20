package com.billsync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.billsync.app.ui.screen.*
import com.billsync.app.ui.theme.BillSyncTheme
import com.billsync.app.viewmodel.BillViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BillSyncTheme {
                BillSyncApp()
            }
        }
    }
}

// 导航路由
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "首页", Icons.Filled.Home, Icons.Outlined.Home)
    data object Bills : Screen("bills", "账单", Icons.Filled.Receipt, Icons.Outlined.Receipt)
    data object Statistics : Screen("statistics", "统计", Icons.Filled.PieChart, Icons.Outlined.PieChart)
    data object Settings : Screen("settings", "设置", Icons.Filled.Settings, Icons.Outlined.Settings)
    data object AddBill : Screen("add_bill", "记账", Icons.Filled.Add, Icons.Outlined.Add)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Bills,
    Screen.Statistics,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSyncApp() {
    val navController = rememberNavController()
    val billViewModel: BillViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.AddBill.route) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentRoute == screen.route) screen.selectedIcon
                                    else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = billViewModel,
                    onAddBill = { navController.navigate(Screen.AddBill.route) }
                )
            }
            composable(Screen.Bills.route) {
                BillListScreen(viewModel = billViewModel)
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = billViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = billViewModel)
            }
            composable(Screen.AddBill.route) {
                AddBillScreen(
                    viewModel = billViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
