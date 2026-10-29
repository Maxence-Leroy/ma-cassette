package com.ragicorp.macassette.screens.operations.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Operation
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun DeleteOperationDialog(
    operations: List<Operation>,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.operations_deleteOperationConfirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        icon = { MaterialSymbol.Filled("delete") },
        title = { Text(pluralStringResource(R.plurals.operations_deleteOperationTitle, operations.size)) },
        text = {
            Text(
                operations.singleOrNull()?.let { stringResource(R.string.operations_deleteOperationMessage, it.label) }
                    ?: pluralStringResource(R.plurals.operations_deleteOperationsMessage, operations.size, operations.size),
            )
        },
    )
}
