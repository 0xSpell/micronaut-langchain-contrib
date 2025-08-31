package de.mxci.micronaut.langchain.contrib.config

import io.micronaut.context.annotation.Requires

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Requires(property = "$CONFIG_BASE.enabled", value = "true")
annotation class RequiresContrib
