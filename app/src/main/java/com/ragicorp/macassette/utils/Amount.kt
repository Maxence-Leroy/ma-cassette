package com.ragicorp.macassette.utils

import java.text.NumberFormat

// Digits with at most one decimal separator (',' for French, '.' for English) and 2 decimals
val AMOUNT_REGEX = Regex("""^\d*([.,]\d{0,2})?$""")

// Formats with the device locale and 2 decimals, dropped for whole amounts unless forced. Grouping is turned off for
// text fields, so the amount can be parsed back.
fun Double.formatAmount(
    isGroupingUsed: Boolean = true,
    isDecimalsForced: Boolean = false,
): String =
    NumberFormat
        .getNumberInstance()
        .also {
            it.isGroupingUsed = isGroupingUsed
            it.minimumFractionDigits = if (isDecimalsForced || this % 1.0 != 0.0) 2 else 0
            it.maximumFractionDigits = 2
        }.format(this)
