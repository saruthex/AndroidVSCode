package com.saruthex.androidvscode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidVSCodeApp(
    onOpenFile: () -> Unit,
    onSaveFile: (String, String) -> Unit,
    openedFileName: String?,
    openedFileContent: String?,
    openedFileId: Long
) {
    var active by remember { mutableStateOf("Explorer") }
    var text by remember(openedFileId) {
        mutableStateOf(openedFileContent ?: "// Create a new file or open one from your phone\n")
    }
    var fileName by remember(openedFileId) {
        mutableStateOf(openedFileName ?: "untitled.txt")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidVSCode") },
                actions = {
                    Button(onClick = onOpenFile, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Open")
                    }
                    Button(onClick = { onSaveFile(fileName, text) }, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Save")
                    }
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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Bg)
        ) {
            if (active == "Explorer") {
                ExplorerPanel(
                    fileName = fileName,
                    onNewFile = {
                        fileName = "untitled.txt"
                        text = ""
                    },
                    onOpenFile = onOpenFile
                )
            } else {
                Text(
                    "$active tools are coming next. The editor and real Android file open/save are working now.",
                    color = TextColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            TabRow(selectedTabIndex = 0, containerColor = Panel) {
                Tab(selected = true, onClick = {}, text = { Text(fileName) })
            }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
                textStyle = TextStyle(color = TextColor),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Bg,
                    unfocusedContainerColor = Bg,
                    focusedBorderColor = Blue,
                    unfocusedBorderColor = Color(0xFF3C3C3C)
                )
            )
        }
    }
}

@Composable
private fun ExplorerPanel(
    fileName: String,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Panel).padding(10.dp)) {
        Text("EXPLORER", color = TextColor)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNewFile) { Text("New File") }
            Button(onClick = onOpenFile) { Text("Open File") }
        }
        Text(
            text = fileName,
            color = TextColor,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clickable { }
        )
    }
}
