package com.ragicorp.macassette.screens.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.budget.BudgetScreen
import com.ragicorp.macassette.screens.distribution.DistributionScreen
import com.ragicorp.macassette.screens.home.views.HomeFabMenu
import com.ragicorp.macassette.screens.home.views.HomeFabMenuItems
import com.ragicorp.macassette.screens.operations.OperationsScreen
import dev.vicart.compose.material.symbols.MaterialSymbol
import kotlinx.serialization.Serializable

@Serializable
data object Home

enum class HomeSubScreens(
    @StringRes val text: Int,
    val icon: String,
) {
    Operations(R.string.home_operationsTitle, "list_alt"),
    Budget(R.string.home_budgetTitle, "money_bag"),
    Distribution(R.string.home_distributionTitle, "pie_chart"),
}

@Composable
fun HomeScreen(
    onImportOperationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedScreen by remember { mutableStateOf(HomeSubScreens.Operations) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                HomeSubScreens.entries.forEach {
                    NavigationBarItem(
                        selected = it == selectedScreen,
                        onClick = { selectedScreen = it },
                        icon = { MaterialSymbol.Filled(it.icon) },
                        label = { Text(stringResource(it.text)) },
                    )
                }
            }
        },
        floatingActionButton = {
            HomeFabMenu(
                onItemClick = {
                    when (it) {
                        HomeFabMenuItems.AddOperation -> Unit
                        HomeFabMenuItems.ImportOperations -> onImportOperationsClick()
                    }
                },
            )
        },
    ) { paddingValues ->
        val contentModifier = Modifier.padding(paddingValues)
        when (selectedScreen) {
            HomeSubScreens.Operations -> OperationsScreen(modifier = contentModifier)
            HomeSubScreens.Budget -> BudgetScreen(modifier = contentModifier)
            HomeSubScreens.Distribution -> DistributionScreen(modifier = contentModifier)
        }
    }
}
