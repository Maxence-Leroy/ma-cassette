package com.ragicorp.macassette.screens.ruleform

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.ui.views.CategoryDropdown
import dev.vicart.compose.material.symbols.MaterialSymbol
import kotlinx.serialization.Serializable

// A null ruleId creates a new rule, otherwise the rule with this id is edited
@Serializable
data class RuleForm(
    val ruleId: Long?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleFormScreen(
    ruleId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RuleFormViewModel =
        viewModel(
            factory =
                RuleFormViewModel.RuleFormViewModelFactory(
                    application = LocalContext.current.applicationContext as Application,
                    ruleId = ruleId,
                ),
        ),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val textToRecognize by viewModel.textToRecognize
    val categoryToApply by viewModel.categoryToApply
    val canSave by viewModel.canSave

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (ruleId == null) R.string.ruleForm_createTitle else R.string.ruleForm_editTitle)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        MaterialSymbol.Filled("arrow_back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.double_space)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        ) {
            OutlinedTextField(
                value = textToRecognize,
                onValueChange = viewModel::setTextToRecognize,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.ruleForm_textToRecognize)) },
                singleLine = true,
            )
            CategoryDropdown(
                label = stringResource(R.string.ruleForm_categoryToApply),
                categories = categories,
                selectedCategory = categoryToApply,
                onCategorySelect = viewModel::setCategoryToApply,
            )
            Button(
                onClick = { viewModel.save(onSaved = onBack) },
                modifier = Modifier.align(Alignment.End),
                enabled = canSave,
            ) {
                Text(stringResource(R.string.ruleForm_save))
            }
        }
    }
}
