package com.ragicorp.macassette.screens.importoperations

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.importoperations.views.AmbiguousOperationCard
import com.ragicorp.macassette.screens.importoperations.views.LeaveAmbiguousOperationsDialog
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
    onCreateParserClick: (columns: List<String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportOperationsViewModel = viewModel(),
) {
    val fileName by viewModel.fileName
    val availableParsers by viewModel.availableParsers.collectAsStateWithLifecycle()
    val parser by viewModel.parser
    val columns by viewModel.columns
    val isImporting by viewModel.isImporting
    val ambiguousOperations by viewModel.ambiguousOperations
    var showLeaveDialog by rememberSaveable { mutableStateOf(false) }
    // Leaving drops the operations left to sort, as they aren't saved yet
    val onBackRequest = { if (ambiguousOperations.isEmpty()) onBack() else showLeaveDialog = true }
    val uriHandler = LocalUriHandler.current
    // The system picker grants access to the chosen file, so no storage permission is needed
    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) viewModel.setFile(uri)
        }

    BackHandler(enabled = ambiguousOperations.isNotEmpty()) { showLeaveDialog = true }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val title =
                        if (ambiguousOperations.isEmpty()) R.string.importOperations_title else R.string.importOperations_ambiguousTitle
                    Text(stringResource(title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackRequest) {
                        MaterialSymbol.Filled("arrow_back")
                    }
                },
            )
        },
        floatingActionButton = {
            if (ambiguousOperations.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { if (!isImporting) viewModel.saveAmbiguousOperations(onSaved = onBack) },
                    icon = { MaterialSymbol.Filled("save") },
                    text = { Text(stringResource(R.string.importOperations_save)) },
                )
            }
        },
    ) { paddingValues ->
        if (ambiguousOperations.isNotEmpty()) {
            // Bottom padding keeps the last card clear of the save button
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = dimensionResource(R.dimen.double_space),
                        top = dimensionResource(R.dimen.double_space),
                        end = dimensionResource(R.dimen.double_space),
                        bottom = dimensionResource(R.dimen.fab_clearance),
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.importOperations_ambiguousMessage),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                itemsIndexed(ambiguousOperations) { index, ambiguousOperation ->
                    AmbiguousOperationCard(
                        operation = ambiguousOperation.operation,
                        currency = viewModel.currency,
                        categories = ambiguousOperation.matchingCategories,
                        onCategorySelect = { viewModel.setAmbiguousOperationCategory(index, it) },
                    )
                }
            }
            return@Scaffold
        }
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(dimensionResource(R.dimen.double_space))
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.double_space)),
        ) {
            viewModel.statementUrl?.let { url ->
                OutlinedButton(
                    onClick = { uriHandler.openUri(url) },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    MaterialSymbol.Filled("open_in_new")
                    Text(
                        text = stringResource(R.string.importOperations_downloadStatement),
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.single_space)),
                    )
                }
            }
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
                    // Columns are null while the file is still being read, there's nothing to build a parser from yet
                    onCreateParserClick = { columns?.let(onCreateParserClick) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(
                onClick = { viewModel.import(onImported = onBack) },
                modifier = Modifier.align(Alignment.End),
                enabled = fileName != null && parser != null && !isImporting,
            ) {
                Text(stringResource(R.string.importOperations_import))
            }
        }
    }

    if (showLeaveDialog) {
        LeaveAmbiguousOperationsDialog(
            operationCount = ambiguousOperations.size,
            onConfirm = {
                showLeaveDialog = false
                onBack()
            },
            onDismissRequest = { showLeaveDialog = false },
        )
    }
}
