package com.ragicorp.macassette.screens.home.views

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.ui.views.AnimatedMaterialSymbol
import dev.vicart.compose.material.symbols.MaterialSymbol

enum class HomeFabMenuItems(
    @StringRes val text: Int,
    val icon: String,
) {
    AddOperation(R.string.home_addOperation, "edit_note"),
    ImportOperations(R.string.importOperations_title, "upload_file"),
}

@Composable
fun HomeFabMenu(
    onItemClick: (HomeFabMenuItems) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(expanded) { expanded = false }

    // FloatingActionButtonMenu already pads itself, so undo the Scaffold's own FAB spacing
    val fabSpacing = dimensionResource(R.dimen.double_space)

    FloatingActionButtonMenu(
        modifier = modifier.offset(x = fabSpacing, y = fabSpacing),
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
            ) {
                AnimatedMaterialSymbol(icon = if (checkedProgress > 0.5f) "close" else "add")
            }
        },
    ) {
        HomeFabMenuItems.entries.forEach {
            FloatingActionButtonMenuItem(
                onClick = {
                    expanded = false
                    onItemClick(it)
                },
                icon = { MaterialSymbol.Filled(it.icon) },
                text = { Text(stringResource(it.text)) },
            )
        }
    }
}
