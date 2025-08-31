package de.mxci.micronaut.langchain.contrib.prompt

import io.micronaut.security.authentication.Authentication

data class PromptContext(
    val authentication: Authentication?,
)
