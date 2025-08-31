package de.mxci.micronaut.langchain.contrib.prompt

interface PromptSectionProvider {

    companion object {
        const val DEFAULT_ORDER = 100
    }

    fun order(): Int = DEFAULT_ORDER

    fun provide(ctx: PromptContext): String
}