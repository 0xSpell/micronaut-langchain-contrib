package de.mxci.micronaut.langchain.contrib.prompt.providers

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.prompt.PromptContext
import de.mxci.micronaut.langchain.contrib.prompt.PromptSectionProvider
import de.mxci.micronaut.langchain.contrib.tools.ToolDescriptionBuilder
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentDescriptionProcessor
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class DescriptionFacetsProvider(
    private val toolDescriptionBuilders: List<ToolDescriptionBuilder>,
    private val argumentDescriptionProcessors: List<ArgumentDescriptionProcessor>,
) : PromptSectionProvider {
    override fun provide(ctx: PromptContext): String = buildString {
        appendLine("Tool descriptions are presented as a JSON object of the format:")
        appendLine("{ 'facet': 'value' }")
        appendLine("The 'facet' value further clarifies the purpose of this part of the description")
        appendLine("The value is the actual content.")
        appendLine("The following values can be encountered for facet:")
        val allFacets = (
            toolDescriptionBuilders.flatMap { it.facets() } +
                argumentDescriptionProcessors.flatMap { it.facets() }
            ).toSet()
        allFacets.forEach { facet ->
            appendLine("${facet.name}: ${facet.description}")
        }
    }
}