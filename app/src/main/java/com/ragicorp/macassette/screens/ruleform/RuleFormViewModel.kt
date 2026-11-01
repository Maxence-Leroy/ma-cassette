package com.ragicorp.macassette.screens.ruleform

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.operation.SortingRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RuleFormViewModel(
    application: Application,
    private val ruleId: Long?,
    textToRecognize: String?,
    categoryToApplyId: Long?,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val categories: StateFlow<List<Category>> =
        operationRepository
            .getCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _textToRecognize = mutableStateOf(textToRecognize.orEmpty())
    val textToRecognize: State<String> = _textToRecognize

    private val _categoryToApply = mutableStateOf<Category?>(null)
    val categoryToApply: State<Category?> = _categoryToApply

    val canSave: State<Boolean> =
        derivedStateOf { _textToRecognize.value.isNotBlank() && _categoryToApply.value != null }

    init {
        if (ruleId != null) {
            viewModelScope.launch {
                operationRepository.getSortingRule(ruleId)?.let { rule ->
                    _textToRecognize.value = rule.textToRecognize
                    _categoryToApply.value = rule.categoryToApply
                }
            }
        } else if (categoryToApplyId != null) {
            viewModelScope.launch { _categoryToApply.value = operationRepository.getCategory(categoryToApplyId) }
        }
    }

    fun setTextToRecognize(textToRecognize: String) {
        _textToRecognize.value = textToRecognize
    }

    fun setCategoryToApply(categoryToApply: Category) {
        _categoryToApply.value = categoryToApply
    }

    fun save(onSaved: () -> Unit) {
        val categoryToApply = _categoryToApply.value ?: return
        if (_textToRecognize.value.isBlank()) return
        val rule =
            SortingRule(
                id = ruleId ?: 0,
                textToRecognize = _textToRecognize.value.trim(),
                categoryToApply = categoryToApply,
            )
        viewModelScope.launch {
            if (ruleId == null) {
                operationRepository.insertSortingRule(rule)
            } else {
                operationRepository.updateSortingRule(rule)
            }
            onSaved()
        }
    }

    // The default viewModel() factory can only pass the Application, so this one also passes the id of the rule being
    // edited from the route (null when creating a new one), or the values to prefill a new rule with
    class RuleFormViewModelFactory(
        private val application: Application,
        private val ruleId: Long?,
        private val textToRecognize: String?,
        private val categoryToApplyId: Long?,
    ) : ViewModelProvider.NewInstanceFactory() {
        // Only ever asked for RuleFormViewModel, so the cast to T is safe
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RuleFormViewModel(application, ruleId, textToRecognize, categoryToApplyId) as T
    }
}
