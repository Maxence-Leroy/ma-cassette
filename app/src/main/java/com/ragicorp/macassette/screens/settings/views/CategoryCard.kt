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
import java.text.NumberFormat

@Composable
fun CategoryCard(
    category: Category,
    currency: String,
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
            modifier = Modifier.padding(dimensionResource(R.dimen.double_space)),
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
            Column {
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
        }
    }
}
