package com.ragicorp.macassette.screens.operations

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.operations.views.DeleteOperationDialog
import com.ragicorp.macassette.screens.operations.views.OperationCard
import com.ragicorp.macassette.screens.operations.views.OperationDateHeader
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val SWIPED_OPERATION_TIMEOUT = 5.seconds

@Composable
fun OperationsScreen(
    modifier: Modifier = Modifier,
    viewModel: OperationsViewModel = viewModel(),
) {
    val operations by viewModel.operations.collectAsStateWithLifecycle()
    val operationsByDate = remember(operations) { operations.groupBy { it.date } }
    val listState = rememberLazyListState()
    var swipedOperationId by remember { mutableStateOf<Long?>(null) }
    var pressedOperationId by remember { mutableStateOf<Long?>(null) }
    var operationIdToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(swipedOperationId) {
        if (swipedOperationId != null) {
            delay(SWIPED_OPERATION_TIMEOUT)
            swipedOperationId = null
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { if (it) swipedOperationId = null }
    }

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
            state = listState,
            modifier =
                modifier
                    .fillMaxSize()
                    // Main pass runs after the cards' Initial pass, so a touch on the swiped card itself is already marked
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            if (pressedOperationId != swipedOperationId) swipedOperationId = null
                            pressedOperationId = null
                        }
                    },
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
                        isSwiped = swipedOperationId == operation.id,
                        onSwipedChange = { isSwiped ->
                            if (isSwiped) {
                                swipedOperationId = operation.id
                            } else if (swipedOperationId == operation.id) {
                                swipedOperationId = null
                            }
                        },
                        onEditClick = {},
                        onDeleteClick = { operationIdToDelete = operation.id },
                        modifier =
                            Modifier
                                .pointerInput(operation.id) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                        pressedOperationId = operation.id
                                    }
                                }.run { if (index > 0) overlapPreviousBorder(cardBorder) else this }
                                .padding(horizontal = dimensionResource(R.dimen.double_space)),
                    )
                }
            }
        }
    }

    operations.find { it.id == operationIdToDelete }?.let { operation ->
        DeleteOperationDialog(
            operation = operation,
            onConfirm = {
                viewModel.deleteOperation(operation)
                operationIdToDelete = null
            },
            onDismissRequest = { operationIdToDelete = null },
        )
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
