package com.ragicorp.macassette.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFERENCES_NAME = "preferences"
private const val CURRENCY_KEY = "currency"
private const val DEFAULT_CURRENCY = "€"

object Preferences {
    private fun get(context: Context): SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getCurrency(context: Context): String = get(context).getString(CURRENCY_KEY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY

    fun setCurrency(
        context: Context,
        currency: String,
    ) {
        get(context).edit {
            if (currency.isBlank()) remove(CURRENCY_KEY) else putString(CURRENCY_KEY, currency)
        }
    }
}
