package com.ragicorp.macassette.screens.home

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.budget.BudgetScreen
import com.ragicorp.macassette.screens.distribution.DistributionScreen
import com.ragicorp.macassette.screens.home.operationform.OperationFormBottomSheet
import com.ragicorp.macassette.screens.home.views.HomeFabMenu
import com.ragicorp.macassette.screens.home.views.HomeFabMenuItems
import com.ragicorp.macassette.screens.operations.OperationsScreen
import com.ragicorp.macassette.screens.operations.OperationsViewModel
import com.ragicorp.macassette.screens.operations.views.DeleteOperationDialog
import dev.vicart.compose.material.symbols.MaterialSymbol
import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
enum class HomeSubScreens(
    @StringRes val text: Int,
    val icon: String,
) {
    Operations(R.string.home_operationsTitle, "list_alt"),
    Budget(R.string.home_budgetTitle, "money_bag"),
    Distribution(R.string.home_distributionTitle, "pie_chart"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onImportOperationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Same instance as OperationsScreen's (both are scoped to the Home destination), so the top bar can act on the
    // operations selected in the list
    operationsViewModel: OperationsViewModel = viewModel(),
) {
    var selectedScreen by rememberSerializable { mutableStateOf(HomeSubScreens.Operations) }
    var showAddOperationSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteSelectedOperationsDialog by rememberSaveable { mutableStateOf(false) }
    val operations by operationsViewModel.operations.collectAsStateWithLifecycle()
    val selectedOperationIds by operationsViewModel.selectedOperationIds
    val isSelectionMode = selectedOperationIds.isNotEmpty()

    BackHandler(enabled = isSelectionMode) { operationsViewModel.clearSelection() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(pluralStringResource(R.plurals.home_selectedOperations, selectedOperationIds.size, selectedOperationIds.size))
                    },
                    navigationIcon = {
                        IconButton(onClick = operationsViewModel::clearSelection) {
                            MaterialSymbol.Filled("close")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteSelectedOperationsDialog = true }) {
                            MaterialSymbol.Filled("delete")
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            MaterialSymbol.Filled("settings")
                        }
                    },
                )
            }
        },
        bottomBar = {
            NavigationBar {
                HomeSubScreens.entries.forEach {
                    NavigationBarItem(
                        selected = it == selectedScreen,
                        onClick = {
                            selectedScreen = it
                            operationsViewModel.clearSelection()
                        },
                        icon = { MaterialSymbol.Filled(it.icon) },
                        label = { Text(stringResource(it.text)) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                HomeFabMenu(
                    onItemClick = {
                        when (it) {
                            HomeFabMenuItems.AddOperation -> showAddOperationSheet = true
                            HomeFabMenuItems.ImportOperations -> onImportOperationsClick()
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        val contentModifier = Modifier.padding(paddingValues)
        when (selectedScreen) {
            HomeSubScreens.Operations -> OperationsScreen(modifier = contentModifier)
            HomeSubScreens.Budget -> BudgetScreen(modifier = contentModifier)
            HomeSubScreens.Distribution -> DistributionScreen(modifier = contentModifier)
        }
    }

    if (showAddOperationSheet) {
        // Scope the ViewModel to the sheet so the form starts empty every time it opens
        CompositionLocalProvider(LocalViewModelStoreOwner provides rememberViewModelStoreOwner()) {
            OperationFormBottomSheet(onDismissRequest = { showAddOperationSheet = false })
        }
    }

    operations
        .filter { it.id in selectedOperationIds }
        .takeIf { showDeleteSelectedOperationsDialog && it.isNotEmpty() }
        ?.let { selectedOperations ->
            DeleteOperationDialog(
                operations = selectedOperations,
                onConfirm = {
                    operationsViewModel.deleteSelectedOperations()
                    showDeleteSelectedOperationsDialog = false
                },
                onDismissRequest = { showDeleteSelectedOperationsDialog = false },
            )
        }
}
