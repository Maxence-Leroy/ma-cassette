package com.ragicorp.macassette.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.BuildConfig
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.settings.views.CategoriesSetting
import com.ragicorp.macassette.screens.settings.views.CurrencySetting
import com.ragicorp.macassette.screens.settings.views.ParsersSetting
import com.ragicorp.macassette.screens.settings.views.SortingRulesSetting
import com.ragicorp.macassette.screens.settings.views.StatementUrlSetting
import com.ragicorp.macassette.screens.settings.views.VersionSetting
import dev.vicart.compose.material.symbols.MaterialSymbol
import kotlinx.serialization.Serializable

@Serializable
data object Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAddSortingRuleClick: () -> Unit,
    onEditSortingRuleClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val currency by viewModel.currency
    val statementUrl by viewModel.statementUrl
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val parsers by viewModel.parsers.collectAsStateWithLifecycle()
    val sortingRules by viewModel.sortingRules.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        MaterialSymbol.Filled("arrow_back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            CurrencySetting(
                currency = currency,
                onCurrencyChange = viewModel::setCurrency,
            )
            StatementUrlSetting(
                statementUrl = statementUrl,
                onStatementUrlChange = viewModel::setStatementUrl,
            )
            CategoriesSetting(
                categories = categories,
                currency = currency,
                onDeleteCategory = viewModel::deleteCategory,
            )
            SortingRulesSetting(
                sortingRules = sortingRules,
                onAddClick = onAddSortingRuleClick,
                onEditClick = { onEditSortingRuleClick(it.id) },
                onDeleteSortingRule = viewModel::deleteSortingRule,
            )
            ParsersSetting(
                parsers = parsers,
                onDeleteParser = viewModel::deleteParser,
            )
            VersionSetting(
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
            )
        }
    }
}
