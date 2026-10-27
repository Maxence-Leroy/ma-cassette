package com.ragicorp.macassette.screens.operations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.operations.views.OperationCard
import com.ragicorp.macassette.screens.operations.views.OperationDateHeader

@Composable
fun OperationsScreen(
    modifier: Modifier = Modifier,
    viewModel: OperationsViewModel = viewModel(),
) {
    val operations by viewModel.operations
    val operationsByDate = remember(operations) { operations.groupBy { it.date } }

    if (operations.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.operations_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            // Bottom padding keeps the last card clear of the FAB menu
            contentPadding = PaddingValues(bottom = dimensionResource(R.dimen.fab_clearance)),
        ) {
            operationsByDate.forEach { (date, operationsOfDay) ->
                stickyHeader(key = "date_${date.toEpochDay()}") {
                    OperationDateHeader(date = date)
                }
                itemsIndexed(operationsOfDay, key = { _, operation -> operation.id }) { index, operation ->
                    val cardBorder = dimensionResource(R.dimen.card_border)
                    OperationCard(
                        operation = operation,
                        currency = viewModel.currency,
                        modifier =
                            Modifier
                                .then(if (index > 0) Modifier.overlapPreviousBorder(cardBorder) else Modifier)
                                .padding(horizontal = dimensionResource(R.dimen.double_space)),
                    )
                }
            }
        }
    }
}

// Pulls the card up by its border width so touching cards share a single border line instead of doubling it
private fun Modifier.overlapPreviousBorder(border: Dp) =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val overlap = border.roundToPx()
        layout(placeable.width, placeable.height - overlap) {
            placeable.place(0, -overlap)
        }
    }
