package com.ragicorp.macassette.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.vicart.compose.material.symbols.MaterialSymbol
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy")

@Composable
fun MonthSelector(
    month: YearMonth,
    canSelectPrevious: Boolean,
    canSelectNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = remember(month) { month.format(MONTH_FORMATTER).replaceFirstChar { it.titlecase() } }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousClick, enabled = canSelectPrevious) {
            MaterialSymbol.Filled("chevron_left")
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNextClick, enabled = canSelectNext) {
            MaterialSymbol.Filled("chevron_right")
        }
    }
}
