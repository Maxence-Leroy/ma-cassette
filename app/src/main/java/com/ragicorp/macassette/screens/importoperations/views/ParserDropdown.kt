package com.ragicorp.macassette.screens.importoperations.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Parser
import dev.vicart.compose.material.symbols.MaterialSymbol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParserDropdown(
    parsers: List<Parser>,
    selectedParser: Parser?,
    onParserSelect: (Parser) -> Unit,
    isError: Boolean,
    onCreateParserClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
    ) {
        ParserMenu(
            parsers = parsers,
            selectedParser = selectedParser,
            onParserSelect = onParserSelect,
            isError = isError,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f),
        )
        // Top-aligned with an offset rather than centered, so the error text under the field doesn't move the button
        IconButton(
            onClick = onCreateParserClick,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.outlined_field_icon_offset)),
        ) {
            MaterialSymbol.Filled("add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParserMenu(
    parsers: List<Parser>,
    selectedParser: Parser?,
    onParserSelect: (Parser) -> Unit,
    isError: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
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
            onDismissRequest = { onExpandedChange(false) },
        ) {
            parsers.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onParserSelect(it)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}
