package com.saruthex.androidvscode.core.command

class CommandService {
    private val commands = linkedMapOf<String, () -> Unit>()

    fun register(id: String, action: () -> Unit) {
        commands[id] = action
    }

    fun execute(id: String): Boolean {
        val action = commands[id] ?: return false
        action()
        return true
    }

    fun ids(): List<String> = commands.keys.toList()
}
