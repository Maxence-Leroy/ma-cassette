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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.ragicorp.macassette.screens.operations.views.CreateRuleDialog
import com.ragicorp.macassette.screens.operations.views.DateFilterField
import com.ragicorp.macassette.screens.operations.views.DeleteOperationDialog
import com.ragicorp.macassette.screens.operations.views.OperationCard
import com.ragicorp.macassette.screens.operations.views.OperationDateHeader
import com.ragicorp.macassette.screens.operations.views.QuickFilters
import com.ragicorp.macassette.utils.collapsible
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDate
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

private val SWIPED_OPERATION_TIMEOUT = 5.seconds

@Composable
fun OperationsScreen(
    onCreateRuleClick: (textToRecognize: String, categoryToApplyId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OperationsViewModel = viewModel(),
) {
    val operations by viewModel.operations.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val firstOperationDate by viewModel.firstOperationDate.collectAsStateWithLifecycle()
    val uncategorizedCount by viewModel.uncategorizedCount.collectAsStateWithLifecycle()
    val waitingForActionCount by viewModel.waitingForActionCount.collectAsStateWithLifecycle()
    val selectedCategoryIds by viewModel.selectedCategoryIds
    val startDate by viewModel.startDate
    val endDate by viewModel.endDate
    val isUncategorizedOnly by viewModel.isUncategorizedOnly
    val isWaitingForActionOnly by viewModel.isWaitingForActionOnly
    val hasFilters by viewModel.hasFilters
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val selectedOperationIds by viewModel.selectedOperationIds
    val operationsByDate = remember(operations) { operations.groupBy { it.date } }
    val listState = rememberLazyListState()
    var swipedOperationId by remember { mutableStateOf<Long?>(null) }
    var pressedOperationId by remember { mutableStateOf<Long?>(null) }
    var operationIdToEdit by rememberSaveable { mutableStateOf<Long?>(null) }
    var operationIdToDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    // Kept apart from the operation, which may no longer be listed once categorized (e.g. when only uncategorized ones are)
    var ruleSuggestion by rememberSerializable { mutableStateOf<RuleSuggestion>(RuleSuggestion.Hidden) }

    LaunchedEffect(swipedOperationId) {
        if (swipedOperationId != null) {
            delay(SWIPED_OPERATION_TIMEOUT)
            swipedOperationId = null
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { if (it) swipedOperationId = null }
    }
    // The list keeps its first visible operation in place when its content changes, so go back to the top once the
    // operations of new filters are loaded rather than as soon as the filters change
    val filters = listOf(selectedCategoryIds, startDate, endDate, isUncategorizedOnly, isWaitingForActionOnly)
    var listedFilters by remember { mutableStateOf(filters) }
    LaunchedEffect(operations) {
        if (filters != listedFilters) {
            listedFilters = filters
            listState.scrollToItem(0)
        }
    }

    // The filters slide away while scrolling down and come back as soon as scrolling up, like a collapsing app bar.
    // The scroll moving them is consumed, so the list follows the finger instead of scrolling under them at the same time.
    // Hiding them makes the list taller by as much, so they only hide as far as the list can still scroll: past its end,
    // the list would scroll its content back down to fill the gap, against the finger.
    var filtersHeight by remember { mutableIntStateOf(0) }
    var filtersOffset by remember { mutableFloatStateOf(0f) }
    val filtersScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val minOffset = maxOf(-filtersHeight.toFloat(), filtersOffset - listState.remainingForwardScroll())
                    val newOffset = (filtersOffset + available.y).coerceIn(minOffset, 0f)
                    val consumed = newOffset - filtersOffset
                    filtersOffset = newOffset
                    return Offset(0f, consumed)
                }
            }
        }
    // Without a list to drag, the filters couldn't be brought back, so show them again
    LaunchedEffect(operations.isEmpty()) {
        if (operations.isEmpty()) filtersOffset = 0f
    }

    Column(modifier = modifier.fillMaxSize().nestedScroll(filtersScrollConnection)) {
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
        val filtersModifier =
            Modifier
                .collapsible(offset = { filtersOffset.roundToInt() }, onHeightChange = { filtersHeight = it })
                .padding(horizontal = dimensionResource(R.dimen.double_space))
        val filtersSpacing = Arrangement.spacedBy(dimensionResource(R.dimen.single_space))
        Column(modifier = filtersModifier, verticalArrangement = filtersSpacing) {
            if (isLandscape) {
                Row(horizontalArrangement = filtersSpacing) {
                    categoryFilter(Modifier.weight(2f))
                    dateFilters()
                }
            } else {
                categoryFilter(Modifier.fillMaxWidth())
                Row(horizontalArrangement = filtersSpacing, content = dateFilters)
            }
            QuickFilters(
                isUncategorizedOnly = isUncategorizedOnly,
                uncategorizedCount = uncategorizedCount,
                onUncategorizedOnlyToggle = viewModel::toggleUncategorizedOnly,
                isWaitingForActionOnly = isWaitingForActionOnly,
                waitingForActionCount = waitingForActionCount,
                onWaitingForActionOnlyToggle = viewModel::toggleWaitingForActionOnly,
                hasFilters = hasFilters,
                onClearFilters = viewModel::clearFilters,
            )
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
                onSave = { savedOperation ->
                    val category = savedOperation.category
                    if (operation.category == null && category != null) {
                        ruleSuggestion = RuleSuggestion.Shown(textToRecognize = savedOperation.label, categoryToApplyId = category.id)
                    }
                },
            )
        }
    }

    (ruleSuggestion as? RuleSuggestion.Shown)?.let { suggestion ->
        categories.find { it.id == suggestion.categoryToApplyId }?.let { category ->
            CreateRuleDialog(
                textToRecognize = suggestion.textToRecognize,
                category = category,
                onConfirm = {
                    onCreateRuleClick(suggestion.textToRecognize, suggestion.categoryToApplyId)
                    ruleSuggestion = RuleSuggestion.Hidden
                },
                onDismissRequest = { ruleSuggestion = RuleSuggestion.Hidden },
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

@Serializable
private sealed class RuleSuggestion {
    @Serializable
    data object Hidden : RuleSuggestion()

    @Serializable
    data class Shown(
        val textToRecognize: String,
        val categoryToApplyId: Long,
    ) : RuleSuggestion()
}

// How far the list can still scroll down. Items that aren't laid out yet are estimated from the visible ones' average size.
private fun LazyListState.remainingForwardScroll(): Float {
    val layoutInfo = layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val lastItem = visibleItems.maxByOrNull { it.index } ?: return 0f
    val unseenItemsSize = (layoutInfo.totalItemsCount - 1 - lastItem.index) * visibleItems.sumOf { it.size } / visibleItems.size
    return (lastItem.offset + lastItem.size + unseenItemsSize + layoutInfo.afterContentPadding - layoutInfo.viewportEndOffset)
        .coerceAtLeast(0)
        .toFloat()
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
