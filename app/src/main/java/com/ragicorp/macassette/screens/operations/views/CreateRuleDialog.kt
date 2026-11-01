package com.ragicorp.macassette.screens.operations.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Category
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun CreateRuleDialog(
    textToRecognize: String,
    category: Category,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.operations_createRuleConfirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.operations_createRuleDismiss))
            }
        },
        icon = { MaterialSymbol.Filled("rule") },
        title = { Text(stringResource(R.string.operations_createRuleTitle)) },
        text = { Text(stringResource(R.string.operations_createRuleMessage, textToRecognize, category.name)) },
    )
}
