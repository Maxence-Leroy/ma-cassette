package com.ragicorp.macassette.screens.createparser

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.createparser.views.ColumnDropdown
import dev.vicart.compose.material.symbols.MaterialSymbol
import kotlinx.serialization.Serializable

@Serializable
data class CreateParser(
    val columns: List<String>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateParserScreen(
    columns: List<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateParserViewModel =
        viewModel(
            factory =
                CreateParserViewModel.CreateParserViewModelFactory(
                    application = LocalContext.current.applicationContext as Application,
                    columns = columns,
                ),
        ),
) {
    val name by viewModel.name
    val dateLabel by viewModel.dateLabel
    val nameLabel by viewModel.nameLabel
    val amountLabel by viewModel.amountLabel

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.createParser_title)) },
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
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.double_space)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.createParser_name)) },
                singleLine = true,
            )
            ColumnDropdown(
                label = stringResource(R.string.createParser_dateLabel),
                columns = viewModel.columns,
                selectedColumn = dateLabel,
                onColumnSelect = viewModel::setDateLabel,
            )
            ColumnDropdown(
                label = stringResource(R.string.createParser_nameLabel),
                columns = viewModel.columns,
                selectedColumn = nameLabel,
                onColumnSelect = viewModel::setNameLabel,
            )
            ColumnDropdown(
                label = stringResource(R.string.createParser_amountLabel),
                columns = viewModel.columns,
                selectedColumn = amountLabel,
                onColumnSelect = viewModel::setAmountLabel,
            )
        }
    }
}
