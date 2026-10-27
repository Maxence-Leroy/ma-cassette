package com.ragicorp.macassette.screens.importoperations

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class ImportOperationsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _fileUri = mutableStateOf<Uri?>(null)
    val fileUri: State<Uri?> = _fileUri

    private val _fileName = mutableStateOf<String?>(null)
    val fileName: State<String?> = _fileName

    fun setFile(uri: Uri) {
        _fileUri.value = uri
        _fileName.value =
            getApplication<Application>()
                .contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: uri.lastPathSegment
    }
}
