package de.mxci.micronaut.langchain.contrib.annotation

import io.micronaut.context.annotation.Executable

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Executable(processOnStartup = true)
annotation class AiTool(
    val value: String = ELEMENT_NAME,
    val description: String = "",
) {
    companion object {
        const val ELEMENT_NAME = "<<element_name>>"
    }
}
