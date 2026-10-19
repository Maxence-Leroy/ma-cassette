package com.ragicorp.macassette.screens.settings.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.screens.settings.addcategory.AddCategoryBottomSheet
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun CategoriesSetting(
    categories: List<Category>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showAddCategorySheet by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        ListItem(
            onClick = { expanded = !expanded },
            trailingContent = { MaterialSymbol.Filled(if (expanded) "keyboard_arrow_up" else "keyboard_arrow_down") },
        ) {
            Text(stringResource(R.string.settings_categories))
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.double_space)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
            ) {
                categories.forEach {
                    CategoryCard(category = it, currency = currency)
                }
                TextButton(
                    onClick = { showAddCategorySheet = true },
                    contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
                ) {
                    MaterialSymbol.Filled("add")
                    Text(
                        text = stringResource(R.string.settings_addCategory),
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.single_space)),
                    )
                }
            }
        }
    }

    if (showAddCategorySheet) {
        // Scope the ViewModel to the sheet so the form starts empty every time it opens
        CompositionLocalProvider(LocalViewModelStoreOwner provides rememberViewModelStoreOwner()) {
            AddCategoryBottomSheet(onDismissRequest = { showAddCategorySheet = false })
        }
    }
}
