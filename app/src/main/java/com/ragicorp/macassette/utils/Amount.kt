package com.ragicorp.macassette.utils

// Digits with at most one decimal separator (',' for French, '.' for English) and 2 decimals
val AMOUNT_REGEX = Regex("""^\d*([.,]\d{0,2})?$""")
