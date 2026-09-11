package com.saruthex.androidvscode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(
                background = Color(0xFF1E1E1E),
                surface = Color(0xFF252526)
            )) {
                AndroidVSCodeApp()
            }
        }
    }
}
