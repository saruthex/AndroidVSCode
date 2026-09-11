package com.saruthex.androidvscode.workbench

import androidx.compose.runtime.Immutable

enum class ActivityView { EXPLORER, SEARCH, GIT, RUN, EXTENSIONS }

@Immutable
data class WorkbenchState(
    val activity: ActivityView = ActivityView.EXPLORER,
    val openEditors: List<String> = listOf("Welcome.kt"),
    val activeEditor: String = "Welcome.kt",
    val bottomPanelVisible: Boolean = false
)
