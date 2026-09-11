package com.saruthex.androidvscode.core.workspace

data class Workspace(
    val id: String,
    val name: String,
    val rootUri: String,
    val openedFiles: List<String> = emptyList()
)
