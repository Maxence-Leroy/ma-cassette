package com.ragicorp.macassette.screens.importoperations

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.importoperations.views.ParserDropdown
import dev.vicart.compose.material.symbols.MaterialSymbol
import kotlinx.serialization.Serializable

// Some file providers label CSV files with a non-standard type (bank exports often come as Excel), so accept the common ones
private val CSV_MIME_TYPES =
    arrayOf(
        "text/csv",
        "text/comma-separated-values",
        "application/csv",
        "text/plain",
        "application/vnd.ms-excel",
        "application/x-excel",
    )

@Serializable
data object ImportOperations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportOperationsScreen(
    onBack: () -> Unit,
    onCreateParserClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportOperationsViewModel = viewModel(),
) {
    val fileName by viewModel.fileName
    val availableParsers by viewModel.availableParsers.collectAsStateWithLifecycle()
    val parser by viewModel.parser
    // The system picker grants access to the chosen file, so no storage permission is needed
    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) viewModel.setFile(uri)
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.importOperations_title)) },
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
                    .padding(dimensionResource(R.dimen.double_space))
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.double_space)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.double_space)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                fileName?.let {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(onClick = { filePicker.launch(CSV_MIME_TYPES) }) {
                    Text(stringResource(R.string.importOperations_selectFile))
                }
            }
            if (fileName != null) {
                ParserDropdown(
                    parsers = availableParsers.orEmpty(),
                    selectedParser = parser,
                    onParserSelect = viewModel::setParser,
                    // availableParsers is null while the file is still being read, so only flag an error once it's known
                    isError = availableParsers?.isEmpty() == true,
                    onCreateParserClick = onCreateParserClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
