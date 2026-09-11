package com.saruthex.androidvscode

import androidx.compose.foundation.background
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

private val Bg = Color(0xFF1E1E1E)
private val Panel = Color(0xFF252526)
private val Blue = Color(0xFF007ACC)
private val TextColor = Color(0xFFD4D4D4)
private val LineColor = Color(0xFF858585)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidVSCodeApp(
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onSaveFile: (String, String) -> Unit,
    openedFileName: String?,
    openedFileContent: String?,
    openedFileId: Long
) {
    var active by remember { mutableStateOf("Explorer") }
    var text by remember(openedFileId) { mutableStateOf(openedFileContent ?: "") }
    var fileName by remember(openedFileId) { mutableStateOf(openedFileName ?: "untitled.txt") }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var replaceMode by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }
    var terminalOutput by remember { mutableStateOf("AndroidVSCode terminal\nType help for commands.\n") }
    var terminalCommand by remember { mutableStateOf("") }

    val lineCount = maxOf(1, text.count { it == '\n' } + 1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidVSCode") },
                actions = {
                    Button(onClick = onOpenFile, modifier = Modifier.padding(end = 6.dp)) { Text("Open") }
                    Button(onClick = { onSaveFile(fileName, text); status = "Saved" }, modifier = Modifier.padding(end = 8.dp)) { Text("Save") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Panel)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                listOf("Explorer", "Search", "Git", "Run", "Terminal").forEach { item ->
                    NavigationBarItem(
                        selected = active == item,
                        onClick = { active = item },
                        icon = { Text(item.take(1)) },
                        label = { Text(item) }
                    )
                }
            }
        },
        containerColor = Bg
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Bg)) {
            when (active) {
                "Explorer" -> ExplorerPanel(fileName, onNewFile, onOpenFile)
                "Search" -> SearchPanel(
                    searchQuery = searchQuery,
                    replaceQuery = replaceQuery,
                    replaceMode = replaceMode,
                    onSearchChange = { searchQuery = it },
                    onReplaceChange = { replaceQuery = it },
                    onToggleReplace = { replaceMode = !replaceMode },
                    onReplaceAll = {
                        if (searchQuery.isNotEmpty()) {
                            text = text.replace(searchQuery, replaceQuery)
                            status = "Replaced all matches"
                        }
                    },
                    matchCount = if (searchQuery.isBlank()) 0 else Regex(Regex.escape(searchQuery)).findAll(text).count()
                )
                "Run" -> RunPanel(fileName, text)
                "Terminal" -> TerminalPanel(terminalOutput, terminalCommand, { terminalCommand = it }) { command ->
                    val result = when {
                        command.trim() == "help" -> "help, clear, pwd, echo <text>"
                        command.trim() == "pwd" -> "/AndroidVSCode"
                        command.trim() == "clear" -> ""
                        command.startsWith("echo ") -> command.removePrefix("echo ")
                        else -> "Unknown command: $command"
                    }
                    terminalOutput = if (command.trim() == "clear") "" else terminalOutput + "$ " + command + "\\n" + result + "\\n"
                    terminalCommand = ""
                }
                "Git" -> Text("Git integration comes after workspace support.", color = TextColor, modifier = Modifier.padding(12.dp))
                else -> Text("$active is planned for Phase 3.", color = TextColor, modifier = Modifier.padding(12.dp))
            }

            TabRow(selectedTabIndex = 0, containerColor = Panel) {
                Tab(selected = true, onClick = {}, text = { Text(fileName) })
            }

            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    for (line in 1..lineCount) {
                        Text(line.toString(), color = LineColor)
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; status = "Editing" },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(color = TextColor),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Bg,
                        unfocusedContainerColor = Bg,
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Color(0xFF3C3C3C)
                    )
                )
            }

            HorizontalDivider()
            Text("$status  •  $lineCount lines", color = LineColor, modifier = Modifier.padding(6.dp))
        }
    }
}

@Composable
private fun ExplorerPanel(fileName: String, onNewFile: () -> Unit, onOpenFile: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(10.dp)) {
        Text("EXPLORER", color = TextColor)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row {
            Button(onClick = onNewFile) { Text("New File") }
            Button(onClick = onOpenFile, modifier = Modifier.padding(start = 6.dp)) { Text("Open File") }
            TextButton(onClick = {}) { Text(fileName) }
        }
    }
}

@Composable
private fun SearchPanel(
    searchQuery: String,
    replaceQuery: String,
    replaceMode: Boolean,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onToggleReplace: () -> Unit,
    onReplaceAll: () -> Unit,
    matchCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(10.dp)) {
        Text("SEARCH", color = TextColor)
        OutlinedTextField(searchQuery, onSearchChange, label = { Text("Find") }, modifier = Modifier.fillMaxWidth())
        Text("$matchCount matches", color = LineColor, modifier = Modifier.padding(top = 4.dp))
        TextButton(onClick = onToggleReplace) { Text(if (replaceMode) "Hide Replace" else "Replace") }
        if (replaceMode) {
            OutlinedTextField(replaceQuery, onReplaceChange, label = { Text("Replace with") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = onReplaceAll, modifier = Modifier.padding(top = 6.dp)) { Text("Replace All") }
        }
    }
}


@Composable
private fun RunPanel(fileName: String, source: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(8.dp)) {
        val isHtml = fileName.endsWith(".html", true) || source.contains("<html", true) || source.contains("<!doctype html", true)
        if (isHtml) {
            Text("HTML LIVE PREVIEW", color = TextColor)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(null, source, "text/html", "UTF-8", null)
                }
            )
        } else {
            Text("Run currently supports HTML files.", color = TextColor)
            Text("Open an .html file and tap Run.", color = LineColor)
        }
    }
}


@Composable
private fun TerminalPanel(
    output: String,
    command: String,
    onCommandChange: (String) -> Unit,
    onExecute: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Bg).padding(10.dp)) {
        Text(
            output,
            color = TextColor,
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = command,
                onValueChange = onCommandChange,
                label = { Text("Command") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { if (command.isNotBlank()) onExecute(command) },
                modifier = Modifier.padding(start = 6.dp)
            ) { Text("Run") }
        }
    }
}
