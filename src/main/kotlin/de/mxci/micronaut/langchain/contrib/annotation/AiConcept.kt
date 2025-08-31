package de.mxci.micronaut.langchain.contrib.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AiConcept(
    val name: String = "",
    val description: String,
    val aliases: Array<String> = [],
)

data class Concept(
    val name: String,
    val description: String,
    val aliases: List<String> = emptyList(),
)