package de.mxci.micronaut.langchain.contrib.tools.parameter

import io.micronaut.core.type.Argument

interface InjectedArgumentDetector {
    fun isInjected(arg: Argument<*>): Boolean
}