package com.ragicorp.macassette.screens.importoperations

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.ragicorp.macassette.R

class ImportOperationsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _fileUri = mutableStateOf<Uri?>(null)
    val fileUri: State<Uri?> = _fileUri

    private val _fileName = mutableStateOf<String?>(null)
    val fileName: State<String?> = _fileName

    fun setFile(uri: Uri) {
        val context = getApplication<Application>()
        val name =
            context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: uri.lastPathSegment
        // The picker also lets through other file types that providers label like CSV (e.g. Excel), so check the extension
        if (name?.endsWith(".csv", ignoreCase = true) == true) {
            _fileUri.value = uri
            _fileName.value = name
        } else {
            _fileUri.value = null
            _fileName.value = null
            Toast.makeText(context, context.getString(R.string.importOperations_notCsv), Toast.LENGTH_SHORT).show()
        }
    }
}
