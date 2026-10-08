package com.ragicorp.macassette.screens.home.addoperation

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.utils.Preferences
import org.threeten.bp.LocalDate

// Digits with at most one decimal separator (',' for French, '.' for English) and 2 decimals
private val AMOUNT_REGEX = Regex("""^\d*([.,]\d{0,2})?$""")

class AddOperationViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val currency = Preferences.getCurrency(application)

    val categories = emptyList<Category>()

    private val _date = mutableStateOf(LocalDate.now())
    val date: State<LocalDate> = _date

    private val _category = mutableStateOf<Category?>(null)
    val category: State<Category?> = _category

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _amount = mutableStateOf("")
    val amount: State<String> = _amount

    private val _isEarning = mutableStateOf(false)
    val isEarning: State<Boolean> = _isEarning

    private val _notes = mutableStateOf("")
    val notes: State<String> = _notes

    fun setDate(date: LocalDate) {
        _date.value = date
    }

    fun setCategory(category: Category) {
        _category.value = category
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setAmount(amount: String) {
        if (AMOUNT_REGEX.matches(amount)) _amount.value = amount
    }

    fun setIsEarning(isEarning: Boolean) {
        _isEarning.value = isEarning
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }
}
