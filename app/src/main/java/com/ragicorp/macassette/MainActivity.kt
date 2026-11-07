package com.ragicorp.macassette

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.ragicorp.macassette.utils.checkForAppUpdate

private const val NOTIFICATION_PERMISSION_ASKED_KEY = "notificationPermissionAsked"

class MainActivity : ComponentActivity() {
    // The answer doesn't matter: closing the permission dialog resumes the activity, which checks for updates
    private val notificationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private var notificationPermissionAsked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationPermissionAsked = savedInstanceState?.getBoolean(NOTIFICATION_PERMISSION_ASKED_KEY) ?: false
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NOTIFICATION_PERMISSION_ASKED_KEY, notificationPermissionAsked)
    }

    override fun onResume() {
        super.onResume()
        // App Distribution shows the update download progress in a notification
        if (!notificationPermissionAsked && needsNotificationPermission()) {
            notificationPermissionAsked = true
            notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        checkForAppUpdate()
    }

    // Only release builds declare the permission (through the full App Distribution SDK), so the others are never asked
    private fun needsNotificationPermission() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            packageManager
                .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
                ?.contains(Manifest.permission.POST_NOTIFICATIONS) == true &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
}
