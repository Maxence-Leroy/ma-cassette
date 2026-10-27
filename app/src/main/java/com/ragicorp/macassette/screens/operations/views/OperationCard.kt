package com.ragicorp.macassette.screens.operations.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Operation
import java.text.NumberFormat

@Composable
fun OperationCard(
    operation: Operation,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val amount =
        remember(operation.amount) {
            NumberFormat
                .getNumberInstance()
                .apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }.format(operation.amount)
                .let { if (operation.amount > 0) "+$it" else it }
        }

    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = dimensionResource(R.dimen.double_space),
                    vertical = dimensionResource(R.dimen.single_space),
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.double_space)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.category_color))
                        .clip(CircleShape)
                        .background(operation.category?.let { Color(it.color) } ?: MaterialTheme.colorScheme.outlineVariant),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = operation.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = operation.category?.name ?: stringResource(R.string.operations_noCategory),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (operation.notes.isNotBlank()) {
                    Text(
                        text = operation.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = stringResource(R.string.operations_amount, amount, currency),
                style = MaterialTheme.typography.titleMedium,
                color = if (operation.amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
