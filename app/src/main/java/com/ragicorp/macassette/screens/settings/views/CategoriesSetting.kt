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
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.CategoryType
import com.ragicorp.macassette.screens.settings.categoryform.CategoryFormBottomSheet
import dev.vicart.compose.material.symbols.MaterialSymbol
import kotlinx.serialization.Serializable

@Composable
fun CategoriesSetting(
    categories: List<Category>,
    currency: String,
    onDeleteCategory: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showCategoryBottomSheet by rememberSerializable { mutableStateOf<ShowCategoryBottomSheet>(ShowCategoryBottomSheet.Hidden) }
    var categoryIdToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(modifier = modifier) {
        ListItem(
            onClick = { expanded = !expanded },
            supportingContent = if (expanded) null else ({ Text(categoriesSummary(categories)) }),
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
                    CategoryCard(
                        category = it,
                        currency = currency,
                        onEditClick = { showCategoryBottomSheet = ShowCategoryBottomSheet.Edit(it.id) },
                        onDeleteClick = { categoryIdToDelete = it.id },
                    )
                }
                TextButton(
                    onClick = { showCategoryBottomSheet = ShowCategoryBottomSheet.Add },
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

    val categoryToEdit =
        (showCategoryBottomSheet as? ShowCategoryBottomSheet.Edit)?.let { sheet ->
            categories.find { it.id == sheet.categoryId }
        }
    if (showCategoryBottomSheet == ShowCategoryBottomSheet.Add || categoryToEdit != null) {
        // Scope the ViewModel to the sheet so the form starts empty (or prefilled from the edited category) every time it opens
        CompositionLocalProvider(LocalViewModelStoreOwner provides rememberViewModelStoreOwner()) {
            CategoryFormBottomSheet(
                onDismissRequest = { showCategoryBottomSheet = ShowCategoryBottomSheet.Hidden },
                category = categoryToEdit,
            )
        }
    }

    categories.find { it.id == categoryIdToDelete }?.let { category ->
        DeleteCategoryDialog(
            category = category,
            onConfirm = {
                onDeleteCategory(category)
                categoryIdToDelete = null
            },
            onDismissRequest = { categoryIdToDelete = null },
        )
    }
}

@Serializable
private sealed class ShowCategoryBottomSheet {
    @Serializable
    data object Hidden : ShowCategoryBottomSheet()

    @Serializable
    data object Add : ShowCategoryBottomSheet()

    @Serializable
    data class Edit(
        val categoryId: Long,
    ) : ShowCategoryBottomSheet()
}

@Composable
private fun categoriesSummary(categories: List<Category>): String {
    val expenseCount = categories.count { it.type == CategoryType.Expense }
    val earningCount = categories.count { it.type == CategoryType.Earning }
    val internalCount = categories.count { it.type == CategoryType.Internal }
    return stringResource(
        R.string.settings_categoriesSummary,
        pluralStringResource(R.plurals.settings_expenseCount, expenseCount, expenseCount),
        pluralStringResource(R.plurals.settings_earningCount, earningCount, earningCount),
        pluralStringResource(R.plurals.settings_internalCount, internalCount, internalCount),
    )
}
