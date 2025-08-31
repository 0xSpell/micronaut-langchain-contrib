package de.mxci.micronaut.langchain.contrib.tools

import dev.langchain4j.agent.tool.ToolSpecification
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod

data class ToolEntry(
    val spec: ToolSpecification,
    val beanDefinition: BeanDefinition<*>,
    val method: ExecutableMethod<*, *>,
)