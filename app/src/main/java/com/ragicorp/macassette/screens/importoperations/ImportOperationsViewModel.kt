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
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.operation.OperationState
import com.ragicorp.macassette.operation.Parser
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset

private val CSV_DELIMITERS = listOf(';', ',', '\t')
private val CSV_DATE_FORMATTERS =
    listOf("d/M/yyyy", "yyyy-M-d", "d-M-yyyy", "d.M.yyyy").map(DateTimeFormatter::ofPattern)

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

    private val _isImporting = mutableStateOf(false)
    val isImporting: State<Boolean> = _isImporting

    // Parsers matching the selected file's columns, null until they are known
    @OptIn(ExperimentalCoroutinesApi::class)
    val availableParsers: StateFlow<List<Parser>?> =
        snapshotFlow { _columns.value }
            .flatMapLatest { columns -> columns?.let(operationRepository::getParsers) ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

    fun import(onImported: () -> Unit) {
        val uri = _fileUri.value ?: return
        val parser = _parser.value ?: return
        // A second import running before the first one is saved would see none of its operations and save them twice
        if (_isImporting.value) return
        _isImporting.value = true
        val context = getApplication<Application>()
        viewModelScope.launch {
            try {
                val operations = withContext(Dispatchers.IO) { readOperations(uri, parser) }
                val importedCount = operationRepository.importOperations(operations)
                val message = context.resources.getQuantityString(R.plurals.importOperations_importedCount, importedCount, importedCount)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                onImported()
            } finally {
                _isImporting.value = false
            }
        }
    }

    // Lines whose date or amount can't be read (e.g. a footer with totals) are skipped
    private fun readOperations(
        uri: Uri,
        parser: Parser,
    ): List<Operation> {
        val bytes =
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: IOException) {
                null
            } ?: return emptyList()
        val lines =
            decode(bytes)
                .removePrefix("﻿")
                .lines()
                .map { it.trimEnd('\r') }
                .filter { it.isNotBlank() }
        val header = lines.firstOrNull() ?: return emptyList()
        val delimiter = findDelimiter(header)
        val columns = splitLine(header, delimiter)
        val dateIndex = columns.indexOf(parser.dateLabel)
        val nameIndex = columns.indexOf(parser.nameLabel)
        val amountIndex = columns.indexOf(parser.amountLabel)
        if (dateIndex == -1 || nameIndex == -1 || amountIndex == -1) return emptyList()
        return lines.drop(1).mapNotNull { line ->
            val fields = splitLine(line, delimiter)
            val date = fields.getOrNull(dateIndex)?.let(::parseDate) ?: return@mapNotNull null
            val amount = fields.getOrNull(amountIndex)?.let(::parseAmount) ?: return@mapNotNull null
            Operation(
                id = 0,
                category = null,
                date = date,
                amount = amount,
                label = fields.getOrNull(nameIndex).orEmpty(),
                notes = "",
                isWaitingForAction = false,
                state = OperationState.AutoImported,
            )
        }
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
        return splitLine(header, findDelimiter(header))
    }

    private fun findDelimiter(header: String) = CSV_DELIMITERS.maxBy { delimiter -> header.count { it == delimiter } }

    // Quoted fields may contain the delimiter, and "" inside quotes is an escaped quote
    private fun splitLine(
        line: String,
        delimiter: Char,
    ): List<String> {
        val fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && line.getOrNull(index + 1) == '"' -> {
                    field.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == delimiter && !inQuotes -> {
                    fields.add(field.toString().trim())
                    field.clear()
                }
                else -> field.append(char)
            }
            index++
        }
        fields.add(field.toString().trim())
        return fields
    }

    private fun parseDate(value: String): LocalDate? =
        CSV_DATE_FORMATTERS.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(value, formatter)
            } catch (e: DateTimeParseException) {
                null
            }
        }

    // French exports use ',' as decimal separator and spaces as thousands separator (e.g. "-1 234,56")
    private fun parseAmount(value: String): Double? {
        val amount = value.filterNot { it.isWhitespace() || it == '\u00A0' || it == '\u202F' || it == '+' }
        return if (',' in amount) {
            amount.replace(".", "").replace(',', '.').toDoubleOrNull()
        } else {
            amount.toDoubleOrNull()
        }
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
