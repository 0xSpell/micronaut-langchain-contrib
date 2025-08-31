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
import jakarta.validation.constraints.NotNull

@Singleton
@RequiresContrib
class NullabilityArgumentDescriptionProvider : ArgumentDescriptionProcessor {

    private val nullability = FacetType(
        name = "nullability",
        description = "Whether the parameter is required or optional."
    )

    override fun facets(): List<FacetType> = listOf(nullability)

    override fun process(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet> {
        val required = !arg.isNullable || arg.annotationMetadata.hasAnnotation(NotNull::class.java)
        return listOf(
            DescriptionFacet(if (required) "required" else "optional", nullability.name)
        )
    }
}
