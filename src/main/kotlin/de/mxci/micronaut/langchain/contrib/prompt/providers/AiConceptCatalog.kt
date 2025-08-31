package de.mxci.micronaut.langchain.contrib.prompt.providers

import de.mxci.micronaut.langchain.contrib.annotation.AiConcept
import de.mxci.micronaut.langchain.contrib.annotation.Concept
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import io.micronaut.context.BeanContext
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class AiConceptCatalog(
    private val ctx: BeanContext,
) {
    fun concepts(): List<Concept> {
        val fromBeans = ctx.allBeanDefinitions
            .mapNotNull { it.beanType }
            .distinct()
            .mapNotNull { type ->
                type.getAnnotation(AiConcept::class.java)?.let { ann ->
                    Concept(
                        name = ann.name.ifBlank { type.simpleName },
                        description = ann.description,
                        aliases = ann.aliases.toList()
                    )
                }
            }

        return fromBeans
            .distinctBy { it.name.lowercase() }
            .sortedBy { it.name.lowercase() }
    }
}