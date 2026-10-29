package com.ragicorp.macassette.screens.createparser

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateParserViewModel(
    application: Application,
    val columns: List<String>,
) : AndroidViewModel(application) {
    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _dateLabel = mutableStateOf<String?>(null)
    val dateLabel: State<String?> = _dateLabel

    private val _nameLabel = mutableStateOf<String?>(null)
    val nameLabel: State<String?> = _nameLabel

    private val _amountLabel = mutableStateOf<String?>(null)
    val amountLabel: State<String?> = _amountLabel

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
