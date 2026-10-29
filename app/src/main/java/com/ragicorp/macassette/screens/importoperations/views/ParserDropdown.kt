package com.ragicorp.macassette.screens.importoperations.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Parser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParserDropdown(
    parsers: List<Parser>,
    selectedParser: Parser?,
    onParserSelect: (Parser) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedParser?.name.orEmpty(),
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text(stringResource(R.string.importOperations_parser)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = if (isError) ({ Text(stringResource(R.string.importOperations_noParser)) }) else null,
            isError = isError,
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            parsers.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onParserSelect(it)
                        expanded = false
                    },
                )
            }
        }
    }
}
