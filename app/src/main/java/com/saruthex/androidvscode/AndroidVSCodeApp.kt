package com.saruthex.androidvscode

import androidx.compose.foundation.background
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF111318)
private val Panel = Color(0xFF1B1F27)
private val Blue = Color(0xFF4DA3FF)
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
    val tabs = remember { mutableStateListOf(fileName) }
    var activeTab by remember { mutableStateOf(0) }
    var projectName by remember { mutableStateOf("MyProject") }
    val projectFiles = remember { mutableStateListOf("index.html", "style.css", "script.js") }
    var activeProjectFile by remember { mutableStateOf("index.html") }
    val projectContents = remember { mutableStateMapOf("index.html" to "<!DOCTYPE html>\n<html><head><title>My Project</title><link rel=\"stylesheet\" href=\"style.css\"></head><body><h1>Hello AndroidVSCode!</h1><script src=\"script.js\"></script></body></html>", "style.css" to "body { padding: 24px; }\nh1 { color: #4DA3FF; }", "script.js" to "console.log(\"Project started!\");") }
    var wordWrap by remember { mutableStateOf(true) }
    var fontSize by remember { mutableStateOf(16) }
    val recentFiles = remember { mutableStateOf(listOf(fileName)) }
    var showEditor by remember { mutableStateOf(true) }
    var runOutput by remember { mutableStateOf("Ready to run HTML/JavaScript") }
    var livePreview by remember { mutableStateOf(false) }
    var isDirty by remember { mutableStateOf(false) }

    val lineCount = maxOf(1, text.count { it == '\n' } + 1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidVSCode") },
                actions = {
                    Button(onClick = { if (!tabs.contains(fileName)) tabs.add(fileName); onOpenFile() }, modifier = Modifier.padding(end = 6.dp)) { Text("Open") }
                    Button(onClick = { onSaveFile(fileName, text); projectContents[activeProjectFile] = text; isDirty = false; status = "Saved" }, modifier = Modifier.padding(end = 8.dp)) { Text("Save") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Panel)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                listOf("Explorer", "Search", "Run", "Terminal", "Git", "Settings").forEach { item ->
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
                "Explorer" -> ExplorerPanel(projectName, projectFiles, activeProjectFile, recentFiles.value, { name -> projectName = name }, { selected -> activeProjectFile = selected; fileName = selected; text = projectContents[selected] ?: ""; if (!tabs.contains(selected)) tabs.add(selected); activeTab = tabs.indexOf(selected); status = "Opened: $selected" }, {
                    val candidate = "untitled" + (projectFiles.size + 1) + ".txt"
                    projectFiles.add(candidate)
                    projectContents[candidate] = ""
                    activeProjectFile = candidate
                    fileName = candidate
                    text = ""
                    if (!tabs.contains(candidate)) tabs.add(candidate)
                    activeTab = tabs.indexOf(candidate)
                    status = "Created: " + candidate
                }, onOpenFile)
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
                            projectContents[activeProjectFile] = text
                            isDirty = true
                            status = "Replaced all matches"
                        }
                    },
                    matchCount = if (searchQuery.isBlank()) 0 else Regex(Regex.escape(searchQuery)).findAll(text).count()
                )
                "Run" -> RunPanel(fileName, text, projectContents, runOutput, { runOutput = it }, livePreview, { livePreview = it })
                "Terminal" -> TerminalPanel(terminalOutput, terminalCommand, { terminalCommand = it }) { command ->
                    val parts = command.trim().split(Regex("\\s+"))
                    val cmd = parts.firstOrNull().orEmpty()
                    val target = parts.drop(1).joinToString(" ")
                    val result = when (cmd) {
                        "help" -> "help, clear, pwd, ls, cat <file>, touch <file>, rm <file>, echo <text>"
                        "pwd" -> "/AndroidVSCode/" + projectName
                        "ls" -> projectFiles.joinToString("\n")
                        "cat" -> projectContents[target] ?: "file not found: " + target
                        "touch" -> if (target.isBlank()) "usage: touch <file>" else if (projectFiles.contains(target)) "already exists: " + target else { projectFiles.add(target); projectContents[target] = ""; "created " + target }
                        "rm" -> if (target.isBlank()) "usage: rm <file>" else if (projectFiles.remove(target)) { projectContents.remove(target); "removed " + target } else "file not found: " + target
                        "echo" -> target
                        "clear" -> ""
                        else -> "Unknown command: " + command
                    }
                    terminalOutput = if (cmd == "clear") "" else terminalOutput + "$ " + command + "\n" + result + "\n"
                                        terminalCommand = ""
                }
                "Git" -> GitPanel(fileName)
                "Settings" -> SettingsPanel(wordWrap, fontSize, { wordWrap = it }, { fontSize = it })
                else -> Text("$active is planned for Phase 3.", color = TextColor, modifier = Modifier.padding(12.dp))
            }

            Surface(color = Panel) {
                TabRow(selectedTabIndex = activeTab.coerceIn(0, maxOf(0, tabs.size - 1)), containerColor = Panel) {
                    tabs.forEachIndexed { index, tabName ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index; activeProjectFile = tabName; fileName = tabName; text = projectContents[tabName] ?: text; status = "Tab: $tabName" },
                        text = { Text(if (tabName == fileName) "$tabName •" else tabName) }
                    )
                }
            }
            }

            if (showEditor) Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    for (line in 1..lineCount) {
                        Text(line.toString(), color = LineColor)
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; projectContents[activeProjectFile] = it; isDirty = true; status = "Editing" },
                    modifier = Modifier.fillMaxSize().weight(1f),
                    textStyle = TextStyle(color = TextColor, fontSize = fontSize.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Bg,
                        unfocusedContainerColor = Bg,
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = Color(0xFF3C3C3C)
                    )
                )
            }

            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                Text("$status  •  $lineCount lines", color = LineColor, modifier = Modifier.weight(1f))
                TextButton(onClick = { showEditor = !showEditor }) { Text(if (showEditor) "Focus" else "Show Editor") }
            }
        }
    }
}

@Composable
private fun ExplorerPanel(
    projectName: String,
    projectFiles: List<String>,
    activeProjectFile: String,
    recentFiles: List<String>,
    onProjectNameChange: (String) -> Unit,
    onSelectFile: (String) -> Unit,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(10.dp)) {
        Text("EXPLORER", color = TextColor)
        OutlinedTextField(
            value = projectName,
            onValueChange = onProjectNameChange,
            label = { Text("Project name") },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("PROJECT FILES", color = LineColor)
        projectFiles.forEach { name ->
            TextButton(onClick = { onSelectFile(name) }) {
                Text("• $name", color = if (name == activeProjectFile) Blue else TextColor)
            }
        }
        Row(modifier = Modifier.padding(top = 6.dp)) {
            Button(onClick = onNewFile) { Text("New File") }
            Button(onClick = onOpenFile, modifier = Modifier.padding(start = 6.dp)) { Text("Open File") }
        }
        Text("RECENT FILES", color = LineColor, modifier = Modifier.padding(top = 10.dp))
        recentFiles.distinct().take(5).forEach { name ->
            Text("• $name", color = TextColor)
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
private fun RunPanel(
    fileName: String,
    source: String,
    projectContents: Map<String, String>,
    output: String,
    onOutput: (String) -> Unit,
    livePreview: Boolean,
    onLivePreviewChange: (Boolean) -> Unit
) {
    val isHtml = fileName.endsWith(".html", true) ||
        source.contains("<html", true) ||
        source.contains("<!doctype html", true)
    val isJs = fileName.endsWith(".js", true) || source.contains("AndroidVSCode.log(")
    val css = projectContents["style.css"] ?: ""
    val js = projectContents["script.js"] ?: ""
    val previewSource = if (fileName.endsWith(".html", true)) source else (projectContents["index.html"] ?: source)
    val previewHtml = previewSource
        .replace("<link rel=\"stylesheet\" href=\"style.css\">", "<style>" + css + "</style>")
        .replace("<script src=\"script.js\"></script>", "<script>" + js + "</script>")

    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(8.dp)) {
        Text(if (isHtml) "HTML LIVE PREVIEW" else if (isJs) "JAVASCRIPT RUNNER" else "RUN", color = TextColor)

        Row(modifier = Modifier.padding(bottom = 6.dp)) {
            Button(onClick = { onOutput("Running $fileName") }) { Text("Run / Refresh") }
            TextButton(onClick = { onLivePreviewChange(!livePreview) }) {
                Text(if (livePreview) "Live ON" else "Live OFF")
            }
        }

        if (isHtml) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(null, previewHtml, "text/html", "UTF-8", null)
                }
            )
        } else if (isJs) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        addJavascriptInterface(JsConsole(onOutput), "AndroidVSCode")
                        evaluateJavascript("window.console = { log: function(m) { AndroidVSCode.log(String(m)); } };", null)
                    }
                },
                update = { webView ->
                    webView.evaluateJavascript(
                        "try { " + source + " } catch(e) { AndroidVSCode.log('Error: ' + e.message); }",
                        null
                    )
                }
            )
            Text("Output:", color = LineColor)
            Text(output, color = TextColor, modifier = Modifier.padding(top = 4.dp))
        } else {
            Text("Supported runners: HTML and JavaScript.", color = TextColor)
            Text("Open an .html or .js file to run it.", color = LineColor)
        }
    }
}

private class JsConsole(private val onLog: (String) -> Unit) {
    @android.webkit.JavascriptInterface
    fun log(message: String) {
        onLog(message)
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


@Composable
private fun GitPanel(fileName: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(12.dp)) {
        Text("SOURCE CONTROL", color = TextColor)
        Text("Workspace: AndroidVSCode", color = LineColor, modifier = Modifier.padding(top = 6.dp))
        Text("Current file: $fileName", color = TextColor, modifier = Modifier.padding(top = 4.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Bg), modifier = Modifier.padding(top = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Ready for workspace integration", color = TextColor)
                Text("Native commit/push needs a local Git engine and authentication.", color = LineColor)
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    wordWrap: Boolean,
    fontSize: Int,
    onWordWrapChange: (Boolean) -> Unit,
    onFontSizeChange: (Int) -> Unit
) {
    var darkMode by remember { mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(12.dp)) {
        Text("SETTINGS", color = TextColor)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            Text("Dark editor theme", color = TextColor)
            Switch(checked = darkMode, onCheckedChange = { darkMode = it })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            Text("Word wrap", color = TextColor)
            Switch(checked = wordWrap, onCheckedChange = onWordWrapChange)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            Text("Font size: $fontSize", color = TextColor)
            Row {
                Button(onClick = { if (fontSize > 12) onFontSizeChange(fontSize - 1) }) { Text("-") }
                Button(onClick = { if (fontSize < 28) onFontSizeChange(fontSize + 1) }, modifier = Modifier.padding(start = 6.dp)) { Text("+") }
            }
        }
        Text("Editor preferences are applied immediately.", color = LineColor)
    }
}
