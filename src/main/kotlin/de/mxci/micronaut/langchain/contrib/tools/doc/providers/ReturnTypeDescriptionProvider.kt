package de.mxci.micronaut.langchain.contrib.tools.doc.providers

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.ToolDescriptionBuilder
import de.mxci.micronaut.langchain.contrib.tools.doc.DescriptionFacet
import de.mxci.micronaut.langchain.contrib.tools.doc.FacetType
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class ReturnTypeDescriptionProvider : ToolDescriptionBuilder {

    private val returns = FacetType(
        name = "returns",
        description = "Short description of the return type."
    )

    private val returnsRef = FacetType(
        name = "returns_ref",
        description = "Reference to the return type schema."
    )

    override fun facets(): List<FacetType> = listOf(returns, returnsRef)

    override fun build(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet> {
        val rt = method.returnType
        val base = rt.type.simpleName
        val generics = rt.typeParameters.map { it.type.simpleName }
        val text = if (generics.isNotEmpty()) "$base<${generics.joinToString(",")}>" else base

        return if (rt.isVoid) {
            listOf(
                DescriptionFacet(text, returns.name)
            )
        } else {
            listOf(
                DescriptionFacet(text, returns.name),
                /* DescriptionFacet(
                    buildContext.attachAndProcess(
                        beanDefinition, method, Argument.of(rt.type, *rt.typeParameters),
                    ), RETURNS_REF.name
                ) */
            )
        }
    }
}
