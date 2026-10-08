package com.ragicorp.macassette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragicorp.macassette.screens.home.Home
import com.ragicorp.macassette.screens.home.HomeScreen
import com.ragicorp.macassette.screens.importoperations.ImportOperations
import com.ragicorp.macassette.screens.importoperations.ImportOperationsScreen
import com.ragicorp.macassette.screens.settings.Settings
import com.ragicorp.macassette.screens.settings.SettingsScreen
import com.ragicorp.macassette.ui.theme.MaCassetteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaCassetteTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Home) {
                    composable<Home> {
                        HomeScreen(
                            onImportOperationsClick = { navController.navigate(ImportOperations) },
                            onSettingsClick = { navController.navigate(Settings) },
                        )
                    }
                    composable<ImportOperations> {
                        ImportOperationsScreen(onBack = { navController.popBackStack() })
                    }
                    composable<Settings> {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
