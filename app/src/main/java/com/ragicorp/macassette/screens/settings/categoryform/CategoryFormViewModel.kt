package com.ragicorp.macassette.screens.settings.categoryform

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.CategoryType
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.AMOUNT_REGEX
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.launch
import java.text.NumberFormat

private const val MAX_BUDGET = 9_999_999.99

class CategoryFormViewModel(
    application: Application,
    private val editedCategory: Category?,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    private val _name = mutableStateOf(editedCategory?.name ?: "")
    val name: State<String> = _name

    private val _color = mutableStateOf(editedCategory?.color)
    val color: State<Int?> = _color

    private val _budget =
        mutableStateOf(
            editedCategory?.budget?.let {
                NumberFormat
                    .getNumberInstance()
                    .apply {
                        isGroupingUsed = false
                        minimumFractionDigits = if (it % 1.0 == 0.0) 0 else 2
                        maximumFractionDigits = 2
                    }.format(it)
            } ?: "",
        )
    val budget: State<String> = _budget

    private val _type = mutableStateOf(editedCategory?.type ?: CategoryType.Expense)
    val type: State<CategoryType> = _type

    val canSave: State<Boolean> = derivedStateOf { _name.value.isNotBlank() && _color.value != null }

    fun setName(name: String) {
        _name.value = name
    }

    fun setColor(color: Int) {
        _color.value = color
    }

    fun setBudget(budget: String) {
        if (AMOUNT_REGEX.matches(budget) && (budget.replace(',', '.').toDoubleOrNull() ?: 0.0) <= MAX_BUDGET) {
            _budget.value = budget
        }
    }

    fun setType(type: CategoryType) {
        _type.value = type
    }

    fun save(onSaved: () -> Unit) {
        val color = _color.value ?: return
        if (_name.value.isBlank()) return
        val category =
            Category(
                id = editedCategory?.id ?: 0,
                name = _name.value.trim(),
                color = color,
                // The budget field is only shown for expenses, so ignore any value left over from expense mode
                budget = if (_type.value == CategoryType.Expense) _budget.value.replace(',', '.').toDoubleOrNull() else null,
                type = _type.value,
            )
        viewModelScope.launch {
            if (editedCategory == null) {
                operationRepository.insertCategory(category)
            } else {
                operationRepository.updateCategory(category)
            }
            onSaved()
        }
    }

    // The default viewModel() factory can only pass the Application, so this one also passes the category being
    // edited (null when adding a new one). It's only used when the ViewModel is first created: an existing instance
    // in the ViewModelStoreOwner is returned as-is, which is why the sheet gets its own owner each time it opens.
    class CategoryFormViewModelFactory(
        private val application: Application,
        private val editedCategory: Category?,
    ) : ViewModelProvider.NewInstanceFactory() {
        // Only ever asked for CategoryFormViewModel, so the cast to T is safe
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CategoryFormViewModel(application, editedCategory) as T
    }
}
