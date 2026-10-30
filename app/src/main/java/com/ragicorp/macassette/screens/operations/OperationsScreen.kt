package com.ragicorp.macassette.screens.operations

import android.content.res.Configuration
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.home.operationform.OperationFormBottomSheet
import com.ragicorp.macassette.screens.operations.views.CategoryFilter
import com.ragicorp.macassette.screens.operations.views.DateFilterField
import com.ragicorp.macassette.screens.operations.views.DeleteOperationDialog
import com.ragicorp.macassette.screens.operations.views.OperationCard
import com.ragicorp.macassette.screens.operations.views.OperationDateHeader
import kotlinx.coroutines.delay
import org.threeten.bp.LocalDate
import kotlin.time.Duration.Companion.seconds

private val SWIPED_OPERATION_TIMEOUT = 5.seconds

@Composable
fun OperationsScreen(
    modifier: Modifier = Modifier,
    viewModel: OperationsViewModel = viewModel(),
) {
    val operations by viewModel.operations.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val firstOperationDate by viewModel.firstOperationDate.collectAsStateWithLifecycle()
    val selectedCategoryIds by viewModel.selectedCategoryIds
    val startDate by viewModel.startDate
    val endDate by viewModel.endDate
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val selectedOperationIds by viewModel.selectedOperationIds
    val operationsByDate = remember(operations) { operations.groupBy { it.date } }
    val listState = rememberLazyListState()
    var swipedOperationId by remember { mutableStateOf<Long?>(null) }
    var pressedOperationId by remember { mutableStateOf<Long?>(null) }
    var operationIdToEdit by rememberSaveable { mutableStateOf<Long?>(null) }
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

    Column(modifier = modifier.fillMaxSize()) {
        // Portrait stacks the category above the dates, landscape lines all the filters up
        val categoryFilter: @Composable (Modifier) -> Unit = {
            CategoryFilter(
                categories = categories,
                selectedCategoryIds = selectedCategoryIds,
                onCategoryToggle = viewModel::toggleCategoryFilter,
                onClear = viewModel::clearCategoryFilter,
                modifier = it,
            )
        }
        // Each date bounds the other, so the start can't be set after the end
        val dateFilters: @Composable RowScope.() -> Unit = {
            DateFilterField(
                date = startDate,
                label = stringResource(R.string.operations_startDate),
                onDateChange = viewModel::setStartDate,
                minDate = firstOperationDate,
                maxDate = endDate ?: LocalDate.now(),
                modifier = Modifier.weight(1f),
            )
            DateFilterField(
                date = endDate,
                label = stringResource(R.string.operations_endDate),
                onDateChange = viewModel::setEndDate,
                minDate = startDate ?: firstOperationDate,
                maxDate = LocalDate.now(),
                modifier = Modifier.weight(1f),
            )
        }
        val filtersModifier = Modifier.padding(horizontal = dimensionResource(R.dimen.double_space))
        val filtersSpacing = Arrangement.spacedBy(dimensionResource(R.dimen.single_space))
        if (isLandscape) {
            Row(modifier = filtersModifier, horizontalArrangement = filtersSpacing) {
                categoryFilter(Modifier.weight(2f))
                dateFilters()
            }
        } else {
            Column(modifier = filtersModifier, verticalArrangement = filtersSpacing) {
                categoryFilter(Modifier.fillMaxWidth())
                Row(horizontalArrangement = filtersSpacing, content = dateFilters)
            }
        }
        if (operations.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
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
                            isSelectionMode = selectedOperationIds.isNotEmpty(),
                            isSelected = operation.id in selectedOperationIds,
                            onSelectedChange = {
                                swipedOperationId = null
                                viewModel.toggleOperationSelection(operation)
                            },
                            onEditClick = { operationIdToEdit = operation.id },
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
    }

    operations.find { it.id == operationIdToEdit }?.let { operation ->
        // Scope the ViewModel to the sheet so the form is prefilled from the edited operation every time it opens
        CompositionLocalProvider(LocalViewModelStoreOwner provides rememberViewModelStoreOwner()) {
            OperationFormBottomSheet(
                onDismissRequest = { operationIdToEdit = null },
                operation = operation,
            )
        }
    }

    operations.find { it.id == operationIdToDelete }?.let { operation ->
        DeleteOperationDialog(
            operations = listOf(operation),
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
