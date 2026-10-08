package com.ragicorp.macassette.screens.home.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOperationBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(R.dimen.double_space),
                        end = dimensionResource(R.dimen.double_space),
                        bottom = dimensionResource(R.dimen.double_space),
                    ),
        ) {
            Text(
                text = stringResource(R.string.home_addOperation),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
