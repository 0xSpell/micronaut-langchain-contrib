package de.mxci.micronaut.langchain.contrib.tools.parameter

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class DefaultInjectedArgumentNameProvider : InjectedArgumentNameProvider {
    override fun getArgumentNames() = listOf(
        "io.micronaut.security.authentication.Authentication",
        "java.security.Principal",
        "io.micronaut.http.HttpRequest",
        "kotlin.coroutines.Continuation"
    )
}