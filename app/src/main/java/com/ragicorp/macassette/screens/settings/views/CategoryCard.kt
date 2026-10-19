package com.ragicorp.macassette.screens.settings.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
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
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Category
import dev.vicart.compose.material.symbols.MaterialSymbol
import java.text.NumberFormat

@Composable
fun CategoryCard(
    category: Category,
    currency: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val budget =
        remember(category.budget) {
            category.budget?.let {
                NumberFormat
                    .getNumberInstance()
                    .apply {
                        minimumFractionDigits = if (it % 1.0 == 0.0) 0 else 2
                        maximumFractionDigits = 2
                    }.format(it)
            }
        }

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
            Box(
                modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.category_color))
                        .clip(CircleShape)
                        .background(Color(category.color)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text =
                        when {
                            category.isEarning -> stringResource(R.string.settings_categoryEarning)
                            budget != null -> stringResource(R.string.settings_categoryBudget, budget, currency)
                            else -> stringResource(R.string.settings_categoryExpense)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                IconButton(onClick = onEditClick) {
                    MaterialSymbol.Filled("edit")
                }
                IconButton(onClick = onDeleteClick) {
                    MaterialSymbol.Filled("delete")
                }
            }
        }
    }
}
