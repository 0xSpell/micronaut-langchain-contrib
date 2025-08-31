package de.mxci.micronaut.langchain.contrib.tools

interface ToolRegistry {
    fun register(entry: ToolEntry)

    fun get(name: String): ToolEntry?

    fun all(): List<ToolEntry>
}