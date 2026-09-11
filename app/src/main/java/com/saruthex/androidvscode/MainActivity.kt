package com.saruthex.androidvscode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

private val VsCodeDark = darkColorScheme(
    primary = Color(0xFF3794FF),
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF252526),
    onBackground = Color(0xFFD4D4D4),
    onSurface = Color(0xFFD4D4D4)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = VsCodeDark) {
                AndroidVSCodeApp()
            }
        }
    }
}
