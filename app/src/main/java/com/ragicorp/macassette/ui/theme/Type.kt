package com.ragicorp.macassette.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
private val BaseTypography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
     */
    )

// Lets long words that don't fit on a line, e.g. category names in narrow cards, break with a hyphen
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val Typography =
    BaseTypography.run {
        copy(
            displayLarge = displayLarge.hyphenated(),
            displayMedium = displayMedium.hyphenated(),
            displaySmall = displaySmall.hyphenated(),
            headlineLarge = headlineLarge.hyphenated(),
            headlineMedium = headlineMedium.hyphenated(),
            headlineSmall = headlineSmall.hyphenated(),
            titleLarge = titleLarge.hyphenated(),
            titleMedium = titleMedium.hyphenated(),
            titleSmall = titleSmall.hyphenated(),
            bodyLarge = bodyLarge.hyphenated(),
            bodyMedium = bodyMedium.hyphenated(),
            bodySmall = bodySmall.hyphenated(),
            labelLarge = labelLarge.hyphenated(),
            labelMedium = labelMedium.hyphenated(),
            labelSmall = labelSmall.hyphenated(),
            displayLargeEmphasized = displayLargeEmphasized.hyphenated(),
            displayMediumEmphasized = displayMediumEmphasized.hyphenated(),
            displaySmallEmphasized = displaySmallEmphasized.hyphenated(),
            headlineLargeEmphasized = headlineLargeEmphasized.hyphenated(),
            headlineMediumEmphasized = headlineMediumEmphasized.hyphenated(),
            headlineSmallEmphasized = headlineSmallEmphasized.hyphenated(),
            titleLargeEmphasized = titleLargeEmphasized.hyphenated(),
            titleMediumEmphasized = titleMediumEmphasized.hyphenated(),
            titleSmallEmphasized = titleSmallEmphasized.hyphenated(),
            bodyLargeEmphasized = bodyLargeEmphasized.hyphenated(),
            bodyMediumEmphasized = bodyMediumEmphasized.hyphenated(),
            bodySmallEmphasized = bodySmallEmphasized.hyphenated(),
            labelLargeEmphasized = labelLargeEmphasized.hyphenated(),
            labelMediumEmphasized = labelMediumEmphasized.hyphenated(),
            labelSmallEmphasized = labelSmallEmphasized.hyphenated(),
        )
    }

private fun TextStyle.hyphenated() = copy(hyphens = Hyphens.Auto)
