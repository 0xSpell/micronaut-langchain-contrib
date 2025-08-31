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
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Singleton
@RequiresContrib
class ValidationArgumentDescriptionProvider : ArgumentDescriptionProcessor {

    private val validation = FacetType(
        name = "validation",
        description = "Bean Validation constraints."
    )

    override fun facets(): List<FacetType> = listOf(validation)

    override fun process(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet> {
        val md = arg.annotationMetadata
        val out = mutableListOf<String>()

        md.getAnnotation(Min::class.java)?.longValue("value")?.ifPresent { out += "min=$it" }
        md.getAnnotation(Max::class.java)?.longValue("value")?.ifPresent { out += "max=$it" }
        md.getAnnotation(Size::class.java)?.let {
            val min = it.intValue("min").orElse(0)
            val max = it.intValue("max").orElse(Int.MAX_VALUE)
            out += "size[$min..$max]"
        }
        md.getAnnotation(Pattern::class.java)?.stringValue("regexp")?.ifPresent { out += "pattern=$it" }
        md.getAnnotation(Email::class.java)?.let { out += "email" }

        return out.distinct().map { DescriptionFacet(it, validation.name) }
    }
}
