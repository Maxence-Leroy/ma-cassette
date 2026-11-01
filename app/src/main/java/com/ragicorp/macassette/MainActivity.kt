package com.ragicorp.macassette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ragicorp.macassette.screens.createparser.CreateParser
import com.ragicorp.macassette.screens.createparser.CreateParserScreen
import com.ragicorp.macassette.screens.home.Home
import com.ragicorp.macassette.screens.home.HomeScreen
import com.ragicorp.macassette.screens.importoperations.ImportOperations
import com.ragicorp.macassette.screens.importoperations.ImportOperationsScreen
import com.ragicorp.macassette.screens.ruleform.RuleForm
import com.ragicorp.macassette.screens.ruleform.RuleFormScreen
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
                            onCreateRuleClick = { textToRecognize, categoryToApplyId ->
                                navController.navigate(
                                    RuleForm(ruleId = null, textToRecognize = textToRecognize, categoryToApplyId = categoryToApplyId),
                                )
                            },
                        )
                    }
                    composable<ImportOperations> {
                        ImportOperationsScreen(
                            onBack = { navController.popBackStack() },
                            onCreateParserClick = { navController.navigate(CreateParser(it)) },
                        )
                    }
                    composable<CreateParser> { backStackEntry ->
                        CreateParserScreen(
                            columns = backStackEntry.toRoute<CreateParser>().columns,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<RuleForm> { backStackEntry ->
                        val route = backStackEntry.toRoute<RuleForm>()
                        RuleFormScreen(
                            ruleId = route.ruleId,
                            textToRecognize = route.textToRecognize,
                            categoryToApplyId = route.categoryToApplyId,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<Settings> {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onAddSortingRuleClick = {
                                navController.navigate(
                                    RuleForm(ruleId = null, textToRecognize = null, categoryToApplyId = null),
                                )
                            },
                            onEditSortingRuleClick = {
                                navController.navigate(
                                    RuleForm(ruleId = it, textToRecognize = null, categoryToApplyId = null),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
