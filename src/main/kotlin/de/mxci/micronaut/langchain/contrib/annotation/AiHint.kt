package de.mxci.micronaut.langchain.contrib.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AiHint(
    val value: String,
)
