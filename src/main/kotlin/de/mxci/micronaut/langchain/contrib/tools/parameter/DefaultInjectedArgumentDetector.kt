package de.mxci.micronaut.langchain.contrib.tools.parameter

import de.mxci.micronaut.langchain.contrib.annotation.AiSkip
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import io.micronaut.core.type.Argument
import io.micronaut.http.bind.RequestBinderRegistry
import jakarta.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@Singleton
@RequiresContrib
class DefaultInjectedArgumentDetector(
    private val injectedArgumentNameProviders: List<InjectedArgumentNameProvider>,
    private val binderRegistry: RequestBinderRegistry,
) : InjectedArgumentDetector {
    private val argumentNames by lazy { injectedArgumentNameProviders.flatMap { it.getArgumentNames() }.toSet() }

    override fun isInjected(arg: Argument<*>): Boolean {
        val t = arg.type
        return when {
            argumentNames.contains(t.name) -> true
            arg.isAnnotationPresent(AiSkip::class.java) -> true
            else -> {
                /* not perfect, but attempt to find some common context binders */
                val binderOpt = binderRegistry.findArgumentBinder(arg)
                val binder = binderOpt.getOrNull()
                val binderClassName = binder?.javaClass?.name
                return binderClassName?.run {
                    contains(".http.bind.binders.HttpRequest") ||
                        contains(".http.bind.binders.Authentication") ||
                        contains(".http.bind.binders.Principal") ||
                        contains(".http.bind.binders.Locale")
                } ?: false
            }
        }
    }
}