package com.saruthex.androidvscode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Bg = Color(0xFF1E1E1E)
private val Panel = Color(0xFF252526)
private val Blue = Color(0xFF007ACC)

@Composable
fun AndroidVSCodeApp() {
    var active by remember { mutableStateOf("Explorer") }
    var text by remember {
        mutableStateOf("""// Welcome to AndroidVSCode
// A native mobile development workbench.

fun main() {
    println("Hello from AndroidVSCode")
}
""")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidVSCode") },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Bg)
        ) {
            TabRow(selectedTabIndex = 0, containerColor = Panel) {
                Tab(selected = true, onClick = {}, text = { Text("Main.kt") })
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                textStyle = LocalTextStyle.current.copy(color = Color(0xFFD4D4D4)),
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
