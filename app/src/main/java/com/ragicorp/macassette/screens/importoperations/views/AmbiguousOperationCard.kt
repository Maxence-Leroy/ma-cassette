package com.ragicorp.macassette.screens.importoperations.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.ui.views.CategoryDropdown
import com.ragicorp.macassette.utils.formatAmount
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

@Composable
fun AmbiguousOperationCard(
    operation: Operation,
    currency: String,
    categories: List<Category>,
    onCategorySelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = remember(operation.date) { operation.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) }
    val amount =
        remember(operation.amount) {
            operation.amount.formatAmount(isDecimalsForced = true).let { if (operation.amount > 0) "+$it" else it }
        }

    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.double_space)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.double_space))) {
                Text(
                    text = operation.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.operations_amount, amount, currency),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CategoryDropdown(
                label = stringResource(R.string.importOperations_category),
                categories = categories,
                selectedCategory = operation.category,
                onCategorySelect = onCategorySelect,
            )
        }
    }
}
