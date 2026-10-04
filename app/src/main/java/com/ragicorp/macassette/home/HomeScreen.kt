package com.ragicorp.macassette.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import dev.vicart.compose.material.symbols.MaterialSymbol

enum class HomeSubScreens(
    @StringRes val text: Int,
    val icon: String,
) {
    Operations(R.string.home_operationsTitle, "list_alt"),
    Budget(R.string.home_budgetTitle, "money_bag"),
    Distribution(R.string.home_distributionTitle, "pie_chart"),
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                HomeSubScreens.entries.forEach {
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { MaterialSymbol.Filled(it.icon) },
                        label = { Text(stringResource(it.text)) },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues))
    }
}
