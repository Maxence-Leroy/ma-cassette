package com.ragicorp.macassette.screens.operations.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.ragicorp.macassette.R
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

@Composable
fun OperationDateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val text = remember(date) { date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) }

    // Opaque background so cards scrolling underneath the pinned header stay hidden
    Text(
        text = text,
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    horizontal = dimensionResource(R.dimen.double_space),
                    vertical = dimensionResource(R.dimen.single_space),
                ),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}
