package de.mxci.micronaut.langchain.contrib.runtime

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import io.micronaut.core.type.Argument
import jakarta.inject.Singleton
import java.security.Principal

@RequiresContrib
@Singleton
class AuthenticationArgumentResolver : InjectedArgumentResolver {
    override fun supports(arg: Argument<*>): Boolean {
        val t = arg.type
        return t == io.micronaut.security.authentication.Authentication::class.java ||
            t == Principal::class.java
    }

    override fun resolve(arg: Argument<*>, ctx: ToolInvocationContext): Any? {
        val auth = ctx.authentication ?: return null
        return when (arg.type) {
            io.micronaut.security.authentication.Authentication::class.java -> auth
            Principal::class.java -> Principal { auth.name }
            else -> null
        }
    }
}
