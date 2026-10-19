package com.ragicorp.macassette.screens.settings.addcategory

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import com.ragicorp.macassette.ui.theme.CategoryColors
import com.ragicorp.macassette.utils.AMOUNT_REGEX
import com.ragicorp.macassette.utils.Preferences

class AddCategoryViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val currency = Preferences.getCurrency(application)

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _color = mutableIntStateOf(CategoryColors.first().toArgb())
    val color: State<Int> = _color

    private val _budget = mutableStateOf("")
    val budget: State<String> = _budget

    private val _isEarning = mutableStateOf(false)
    val isEarning: State<Boolean> = _isEarning

    fun setName(name: String) {
        _name.value = name
    }

    fun setColor(color: Int) {
        _color.intValue = color
    }

    fun setBudget(budget: String) {
        if (AMOUNT_REGEX.matches(budget)) _budget.value = budget
    }

    fun setIsEarning(isEarning: Boolean) {
        _isEarning.value = isEarning
    }
}
