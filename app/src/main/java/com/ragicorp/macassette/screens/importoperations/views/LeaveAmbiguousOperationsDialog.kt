package com.ragicorp.macassette.screens.importoperations.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun LeaveAmbiguousOperationsDialog(
    operationCount: Int,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.importOperations_leaveConfirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        icon = { MaterialSymbol.Filled("warning") },
        title = { Text(stringResource(R.string.importOperations_leaveTitle)) },
        text = { Text(pluralStringResource(R.plurals.importOperations_leaveMessage, operationCount, operationCount)) },
    )
}
