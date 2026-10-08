package com.ragicorp.macassette.screens.settings.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun CurrencySetting(
    currency: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        ListItem(
            onClick = { expanded = !expanded },
            supportingContent = if (expanded) null else ({ Text(currency) }),
            trailingContent = { MaterialSymbol.Filled(if (expanded) "keyboard_arrow_up" else "keyboard_arrow_down") },
        ) {
            Text(stringResource(R.string.settings_currency))
        }
        AnimatedVisibility(visible = expanded) {
            OutlinedTextField(
                value = currency,
                onValueChange = onCurrencyChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.double_space)),
                singleLine = true,
            )
        }
    }
}
