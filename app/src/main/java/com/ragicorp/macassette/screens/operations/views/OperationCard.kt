package com.ragicorp.macassette.screens.operations.views

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Operation
import dev.vicart.compose.material.symbols.MaterialSymbol
import java.text.NumberFormat
import kotlin.math.roundToInt

@Composable
fun OperationCard(
    operation: Operation,
    currency: String,
    isSwiped: Boolean,
    onSwipedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeState = remember { AnchoredDraggableState(OperationCardSwipe.Closed) }
    val currentOnSwipedChange by rememberUpdatedState(onSwipedChange)

    LaunchedEffect(swipeState) {
        snapshotFlow { swipeState.settledValue }.collect { currentOnSwipedChange(it == OperationCardSwipe.Open) }
    }
    LaunchedEffect(isSwiped) {
        if (!isSwiped && swipeState.settledValue == OperationCardSwipe.Open) swipeState.animateTo(OperationCardSwipe.Closed)
    }
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

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .onSizeChanged { size ->
                        swipeState.updateAnchors(
                            DraggableAnchors {
                                OperationCardSwipe.Closed at 0f
                                OperationCardSwipe.Open at -size.width.toFloat()
                            },
                        )
                    },
        ) {
            IconButton(onClick = {
                onSwipedChange(false)
                onEditClick()
            }) {
                MaterialSymbol.Filled("edit")
            }
            IconButton(onClick = {
                onSwipedChange(false)
                onDeleteClick()
            }) {
                MaterialSymbol.Filled("delete")
            }
        }
        OperationCardContent(
            operation = operation,
            amount = amount,
            currency = currency,
            modifier =
                Modifier
                    // Offset is NaN until the actions have been measured and the anchors set
                    .offset { IntOffset(swipeState.offset.takeIf { !it.isNaN() }?.roundToInt() ?: 0, 0) }
                    .anchoredDraggable(swipeState, Orientation.Horizontal)
                    .pointerInput(isSwiped) {
                        if (isSwiped) detectTapGestures { onSwipedChange(false) }
                    },
        )
    }
}

@Composable
private fun OperationCardContent(
    operation: Operation,
    amount: String,
    currency: String,
    modifier: Modifier = Modifier,
) {
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
                )
                Text(
                    text = operation.category?.name ?: stringResource(R.string.operations_noCategory),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (operation.notes.isNotBlank()) {
                    Text(
                        text = operation.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

private enum class OperationCardSwipe {
    Closed,
    Open,
}
