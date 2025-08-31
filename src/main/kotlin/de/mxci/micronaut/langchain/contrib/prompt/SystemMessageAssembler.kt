package de.mxci.micronaut.langchain.contrib.prompt

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class SystemMessageAssembler(
    providers: List<PromptSectionProvider>,
    private val securityService: SecurityService,
) {
    private val ordered = providers.sortedBy { it.order() }

    fun buildSystemMessage(@Suppress("UnusedParameter") memoryId: Any): String {
        val ctx = PromptContext(
            authentication = securityService.authentication.orElse(null),
        )
        val parts = ordered.mapNotNull { p ->
            p.provide(ctx).trim().takeIf { it.isNotEmpty() }
        }
        return parts.joinToString("\n\n")
    }
}