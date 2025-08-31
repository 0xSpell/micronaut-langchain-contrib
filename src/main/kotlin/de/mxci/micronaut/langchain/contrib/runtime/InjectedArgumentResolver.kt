package de.mxci.micronaut.langchain.contrib.runtime

import io.micronaut.core.type.Argument

/** Per-argument injection hook used by the tool executor. */
interface InjectedArgumentResolver {
    /** true if this resolver supplies a value for the given method argument. */
    fun supports(arg: Argument<*>): Boolean

    /** produce a value for the argument (or null if not available). */
    fun resolve(arg: Argument<*>, ctx: ToolInvocationContext): Any?
}

/** Context available during a tool call. Extend later with locale, tenant, etc. */
data class ToolInvocationContext(
    val authentication: io.micronaut.security.authentication.Authentication?,
)
