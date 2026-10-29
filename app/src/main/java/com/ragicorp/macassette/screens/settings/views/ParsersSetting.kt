package com.ragicorp.macassette.screens.settings.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Parser
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun ParsersSetting(
    parsers: List<Parser>,
    onDeleteParser: (Parser) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var parserIdToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(modifier = modifier) {
        ListItem(
            onClick = { expanded = !expanded },
            supportingContent =
                if (expanded) null else ({ Text(pluralStringResource(R.plurals.settings_parserCount, parsers.size, parsers.size)) }),
            trailingContent = { MaterialSymbol.Filled(if (expanded) "keyboard_arrow_up" else "keyboard_arrow_down") },
        ) {
            Text(stringResource(R.string.settings_parsers))
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.double_space)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
            ) {
                parsers.forEach {
                    ParserCard(
                        parser = it,
                        onDeleteClick = { parserIdToDelete = it.id },
                    )
                }
            }
        }
    }

    parsers.find { it.id == parserIdToDelete }?.let { parser ->
        DeleteParserDialog(
            parser = parser,
            onConfirm = {
                onDeleteParser(parser)
                parserIdToDelete = null
            },
            onDismissRequest = { parserIdToDelete = null },
        )
    }
}
