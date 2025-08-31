package de.mxci.micronaut.langchain.contrib.tools.doc.providers

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.ToolDescriptionBuilder
import de.mxci.micronaut.langchain.contrib.tools.doc.DescriptionFacet
import de.mxci.micronaut.langchain.contrib.tools.doc.FacetType
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class HttpPathDescriptionProvider : ToolDescriptionBuilder {

    companion object {
        private val HTTP_PATH = FacetType(
            name = "http_path",
            description = "Underlying HTTP endpoint path (method + path)."
        )
    }

    override fun facets(): List<FacetType> = listOf(HTTP_PATH)

    override fun build(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet> {
        val controllerBase = beanDefinition.stringValue(Controller::class.java).orElse("")
        val (verb, subPath) = when {
            method.hasDeclaredAnnotation(Get::class.java) ->
                "GET" to method.stringValue(Get::class.java).orElse("")
            method.hasDeclaredAnnotation(Post::class.java) ->
                "POST" to method.stringValue(Post::class.java).orElse("")
            method.hasDeclaredAnnotation(Put::class.java) ->
                "PUT" to method.stringValue(Put::class.java).orElse("")
            method.hasDeclaredAnnotation(Delete::class.java) ->
                "DELETE" to method.stringValue(Delete::class.java).orElse("")
            method.hasDeclaredAnnotation(Patch::class.java) ->
                "PATCH" to method.stringValue(Patch::class.java).orElse("")
            else -> return emptyList()
        }
        val path = normalizePath(controllerBase, subPath)
        return listOf(DescriptionFacet("$verb $path", HTTP_PATH.name))
    }

    private fun normalizePath(a: String, b: String): String {
        val left = a.trim().removeSuffix("/")
        val right = b.trim().removePrefix("/")
        return when {
            left.isBlank() && right.isBlank() -> "/"
            right.isBlank() -> if (left.startsWith("/")) left else "/$left"
            left.isBlank() -> if (right.startsWith("/")) right else "/$right"
            else -> "/${left.removePrefix("/")}/$right"
        }
    }
}
