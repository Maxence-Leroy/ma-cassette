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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.SortingRule
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun SortingRuleCard(
    sortingRule: SortingRule,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        .background(Color(sortingRule.categoryToApply.color)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sortingRule.textToRecognize,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = sortingRule.categoryToApply.name,
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
