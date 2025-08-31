package de.mxci.micronaut.langchain.contrib.tools.parameter

import de.mxci.micronaut.langchain.contrib.tools.doc.DescriptionFacet
import de.mxci.micronaut.langchain.contrib.tools.doc.FacetType
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod

interface ArgumentDescriptionProcessor {

    fun process(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet>

    fun facets(): List<FacetType>
}