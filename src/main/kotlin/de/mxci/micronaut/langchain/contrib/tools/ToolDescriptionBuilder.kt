package de.mxci.micronaut.langchain.contrib.tools

import de.mxci.micronaut.langchain.contrib.tools.doc.DescriptionFacet
import de.mxci.micronaut.langchain.contrib.tools.doc.FacetType
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod

interface ToolDescriptionBuilder {
    fun build(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet>

    fun facets(): List<FacetType>
}