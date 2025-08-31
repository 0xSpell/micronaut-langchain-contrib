package de.mxci.micronaut.langchain.contrib.prompt.providers

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.prompt.PromptContext
import de.mxci.micronaut.langchain.contrib.prompt.PromptSectionProvider
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class ConceptsProvider(
    private val catalog: AiConceptCatalog,
) : PromptSectionProvider {
    override fun order() = 20
    override fun provide(ctx: PromptContext): String {
        val concepts = catalog.concepts()
        if (concepts.isEmpty()) return ""
        val list = concepts.joinToString("\n") { c ->
            val aka = if (c.aliases.isEmpty()) "" else " (aka ${c.aliases.joinToString(", ")})"
            "- ${c.name}$aka: ${c.description}"
        }
        return "Domain concepts (for context, not strict validation):\n$list"
    }
}
