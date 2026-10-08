package com.ragicorp.macassette.screens.settings

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.ragicorp.macassette.utils.Preferences

class SettingsScreenViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _currency = mutableStateOf(Preferences.getCurrency(application))
    val currency: State<String> = _currency

    fun setCurrency(currency: String) {
        _currency.value = currency
        Preferences.setCurrency(getApplication(), currency)
    }
}
