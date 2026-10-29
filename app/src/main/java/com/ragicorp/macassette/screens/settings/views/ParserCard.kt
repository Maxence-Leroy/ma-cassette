package com.ragicorp.macassette.screens.settings.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Parser
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun ParserCard(
    parser: Parser,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            // Icon buttons already have their own touch padding, so the end side needs less
            modifier =
                Modifier.padding(
                    start = dimensionResource(R.dimen.double_space),
                    top = dimensionResource(R.dimen.double_space),
                    end = dimensionResource(R.dimen.single_space),
                    bottom = dimensionResource(R.dimen.double_space),
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.double_space)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parser.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.settings_parserColumns, parser.dateLabel, parser.nameLabel, parser.amountLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                MaterialSymbol.Filled("delete")
            }
        }
    }
}
