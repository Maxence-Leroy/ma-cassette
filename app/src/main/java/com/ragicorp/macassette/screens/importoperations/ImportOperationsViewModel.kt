package com.ragicorp.macassette.screens.importoperations

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.operation.Parser
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset

private val CSV_DELIMITERS = listOf(';', ',', '\t')

class ImportOperationsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    // Null when the URL saved in the settings isn't a web URL a browser can open
    val statementUrl: String? =
        Preferences.getStatementUrl(application).takeIf {
            val uri = it.toUri()
            uri.scheme in listOf("http", "https") && !uri.host.isNullOrBlank()
        }

    private val _fileUri = mutableStateOf<Uri?>(null)
    val fileUri: State<Uri?> = _fileUri

    private val _fileName = mutableStateOf<String?>(null)
    val fileName: State<String?> = _fileName

    // Column names from the selected file's header, null while no file is selected or it's still being read
    private val _columns = mutableStateOf<List<String>?>(null)
    val columns: State<List<String>?> = _columns
    private var columnsJob: Job? = null

    private val _parser = mutableStateOf<Parser?>(null)
    val parser: State<Parser?> = _parser

    // Parsers whose three labels are all columns of the selected file, null until both are known
    val availableParsers: StateFlow<List<Parser>?> =
        combine(operationRepository.getParsers(), snapshotFlow { _columns.value }) { parsers, fileColumns ->
            fileColumns?.let {
                parsers.filter { parser -> listOf(parser.dateLabel, parser.nameLabel, parser.amountLabel).all { it in fileColumns } }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setFile(uri: Uri) {
        val context = getApplication<Application>()
        val name =
            context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: uri.lastPathSegment
        columnsJob?.cancel()
        _columns.value = null
        _parser.value = null
        // The picker also lets through other file types that providers label like CSV (e.g. Excel), so check the extension
        if (name?.endsWith(".csv", ignoreCase = true) == true) {
            _fileUri.value = uri
            _fileName.value = name
            columnsJob = viewModelScope.launch { _columns.value = withContext(Dispatchers.IO) { readColumns(uri) } }
        } else {
            _fileUri.value = null
            _fileName.value = null
            Toast.makeText(context, context.getString(R.string.importOperations_notCsv), Toast.LENGTH_SHORT).show()
        }
    }

    fun setParser(parser: Parser) {
        _parser.value = parser
    }

    private fun readColumns(uri: Uri): List<String> {
        val headerBytes =
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    val stream = input.buffered()
                    generateSequence { stream.read().takeIf { it != -1 } }
                        .takeWhile { it != '\n'.code }
                        .map { it.toByte() }
                        .toList()
                        .toByteArray()
                }
            } catch (e: IOException) {
                null
            } ?: return emptyList()
        val header = decode(headerBytes).removePrefix("\uFEFF").trimEnd('\r')
        val delimiter = CSV_DELIMITERS.maxBy { delimiter -> header.count { it == delimiter } }
        return header.split(delimiter).map { it.trim().removeSurrounding("\"") }
    }

    // Bank exports are often Windows-1252 rather than UTF-8, which would garble accented column names
    private fun decode(bytes: ByteArray): String =
        try {
            Charsets.UTF_8
                .newDecoder()
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (e: CharacterCodingException) {
            String(bytes, Charset.forName("windows-1252"))
        }
}
