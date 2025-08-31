package de.mxci.micronaut.langchain.contrib.tools

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@Singleton
@RequiresContrib
class DefaultToolRegistry : ToolRegistry {
    private val log = LoggerFactory.getLogger(javaClass)

    private val byName = ConcurrentHashMap<String, ToolEntry>()
    private val ordered = mutableListOf<ToolEntry>() // preserve discovery order

    override fun register(entry: ToolEntry) {
        log.info("Registering tool with name ${entry.spec.name()} and description ${entry.spec.description()}")
        log.info("tool is: ${entry.spec}")
        val name = entry.spec.name()
        val prev = byName.putIfAbsent(name, entry)
        require(prev == null) { "Duplicate tool name '$name' registered: ${prev?.method} vs ${entry.method}" }
        ordered += entry
        log.info("Registered tool: {}", name)
    }

    override fun get(name: String): ToolEntry? = byName[name]

    override fun all(): List<ToolEntry> = ordered.toList()
}