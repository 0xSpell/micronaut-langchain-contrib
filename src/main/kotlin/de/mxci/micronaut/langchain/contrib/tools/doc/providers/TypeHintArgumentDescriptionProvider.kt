package de.mxci.micronaut.langchain.contrib.tools.doc.providers

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.doc.DescriptionFacet
import de.mxci.micronaut.langchain.contrib.tools.doc.FacetType
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentDescriptionProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime

@Singleton
@RequiresContrib
class TypeHintArgumentDescriptionProvider : ArgumentDescriptionProcessor {

    private val typeHint = FacetType(
        name = "type_hint",
        description = "Low-level type hint (uuid, date-time, uri, enum values, etc.)."
    )

    override fun facets(): List<FacetType> = listOf(typeHint)

    override fun process(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet> {
        val t = arg.type
        return when {
            t == java.util.UUID::class.java -> listOf(h("string (uuid)"))
            t == java.net.URI::class.java || t == java.net.URL::class.java -> listOf(h("string (uri)"))
            t == LocalDate::class.java -> listOf(h("string (date, ISO-8601)"))
            t == LocalTime::class.java -> listOf(h("string (time, ISO-8601)"))
            t == Instant::class.java || t == OffsetDateTime::class.java ||
                t == ZonedDateTime::class.java || t == LocalDateTime::class.java ->
                listOf(h("string (date-time, ISO-8601)"))
            t.isEnum -> {
                val vals = (t.enumConstants as Array<out Enum<*>>).joinToString(",") { it.name }
                listOf(h("enum { $vals }"))
            }
            else -> emptyList()
        }
    }

    private fun h(v: String) = DescriptionFacet(v, typeHint.name)
}
