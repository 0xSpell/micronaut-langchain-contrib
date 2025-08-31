package de.mxci.micronaut.langchain.contrib.tools

import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod

interface ToolParameterBuilder {
    fun build(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        buildContext: ParamBuildContext,
    ): JsonObjectSchema
}