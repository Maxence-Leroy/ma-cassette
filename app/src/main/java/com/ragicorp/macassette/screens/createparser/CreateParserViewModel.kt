package com.ragicorp.macassette.screens.createparser

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.operation.Parser
import kotlinx.coroutines.launch

class CreateParserViewModel(
    application: Application,
    val columns: List<String>,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _dateLabel = mutableStateOf<String?>(null)
    val dateLabel: State<String?> = _dateLabel

    private val _nameLabel = mutableStateOf<String?>(null)
    val nameLabel: State<String?> = _nameLabel

    private val _amountLabel = mutableStateOf<String?>(null)
    val amountLabel: State<String?> = _amountLabel

    val canSave: State<Boolean> =
        derivedStateOf {
            _name.value.isNotBlank() && _dateLabel.value != null && _nameLabel.value != null && _amountLabel.value != null
        }

    fun setName(name: String) {
        _name.value = name
    }

    fun setDateLabel(dateLabel: String) {
        _dateLabel.value = dateLabel
    }

    fun setNameLabel(nameLabel: String) {
        _nameLabel.value = nameLabel
    }

    fun setAmountLabel(amountLabel: String) {
        _amountLabel.value = amountLabel
    }

    fun save(onSaved: () -> Unit) {
        val dateLabel = _dateLabel.value ?: return
        val nameLabel = _nameLabel.value ?: return
        val amountLabel = _amountLabel.value ?: return
        if (_name.value.isBlank()) return
        val parser =
            Parser(
                name = _name.value.trim(),
                dateLabel = dateLabel,
                nameLabel = nameLabel,
                amountLabel = amountLabel,
            )
        viewModelScope.launch {
            operationRepository.insertParser(parser)
            onSaved()
        }
    }

    // The default viewModel() factory can only pass the Application, so this one also passes the selected CSV's
    // column names from the route
    class CreateParserViewModelFactory(
        private val application: Application,
        private val columns: List<String>,
    ) : ViewModelProvider.NewInstanceFactory() {
        // Only ever asked for CreateParserViewModel, so the cast to T is safe
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CreateParserViewModel(application, columns) as T
    }
}
