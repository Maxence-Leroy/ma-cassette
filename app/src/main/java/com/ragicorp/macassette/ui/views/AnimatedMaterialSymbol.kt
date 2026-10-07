package com.ragicorp.macassette.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.ToggleFloatingActionButtonScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun ToggleFloatingActionButtonScope.AnimatedMaterialSymbol(
    icon: String,
    modifier: Modifier = Modifier,
    iconSize: (Float) -> Dp = ToggleFloatingActionButtonDefaults.iconSize(),
) {
    // MaterialSymbol is a glyph with a fixed font size, so it can't follow the size
    // animateIcon imposes: center it and scale it along the same size curve instead.
    Box(
        modifier = modifier.animateIcon({ checkedProgress }, size = iconSize),
        contentAlignment = Alignment.Center,
    ) {
        MaterialSymbol.Filled(
            icon = icon,
            modifier =
                Modifier.wrapContentSize(unbounded = true).graphicsLayer {
                    val scale = iconSize(checkedProgress) / iconSize(0f)
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}
