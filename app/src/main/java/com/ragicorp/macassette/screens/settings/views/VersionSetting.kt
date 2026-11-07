package com.ragicorp.macassette.screens.settings.views

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R

@Composable
fun VersionSetting(
    versionName: String,
    versionCode: Int,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(stringResource(R.string.settings_version)) },
        supportingContent = { Text(stringResource(R.string.settings_versionValue, versionName, versionCode)) },
    )
}
