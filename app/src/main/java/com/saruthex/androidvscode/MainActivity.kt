package com.saruthex.androidvscode

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import java.io.OutputStreamWriter

private val VsCodeDark = darkColorScheme(
    primary = Color(0xFF3794FF),
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF252526),
    onBackground = Color(0xFFD4D4D4),
    onSurface = Color(0xFFD4D4D4)
)

class MainActivity : ComponentActivity() {
    private val fileName = mutableStateOf<String?>(null)
    private val fileContent = mutableStateOf<String?>(null)
    private val fileId = mutableStateOf(0L)
    private var currentUri: Uri? = null
    private var pendingName = "untitled.txt"
    private var pendingContent = ""

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) openDocument(uri)
        }

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                currentUri = uri
                writeDocument(uri, pendingContent)
                fileName.value = pendingName
                fileContent.value = pendingContent
                fileId.value += 1
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = VsCodeDark) {
                AndroidVSCodeApp(
                    onOpenFile = { openFileLauncher.launch(arrayOf("text/*", "application/json", "application/javascript", "text/html")) },
                    onSaveFile = { name, content -> saveDocument(name, content) },
                    openedFileName = fileName.value,
                    openedFileContent = fileContent.value,
                    openedFileId = fileId.value
                )
            }
        }
    }

    private fun openDocument(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }

        val content = contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: return

        currentUri = uri
        fileName.value = queryDisplayName(uri) ?: "opened-file.txt"
        fileContent.value = content
        fileId.value += 1
    }

    private fun saveDocument(name: String, content: String) {
        pendingName = name.ifBlank { "untitled.txt" }
        pendingContent = content

        val uri = currentUri
        if (uri == null) {
            createFileLauncher.launch(pendingName)
        } else {
            writeDocument(uri, content)
            fileContent.value = content
        }
    }

    private fun writeDocument(uri: Uri, content: String) {
        contentResolver.openOutputStream(uri, "wt")?.use { stream ->
            OutputStreamWriter(stream).use { writer ->
                writer.write(content)
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')
    }
}
