package de.mxci.micronaut.langchain.contrib.tools.parameter

import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import java.util.*

interface ArgumentTypeProcessor {

    companion object {
        const val DEFAULT_PRIORITY = 1000
    }

    fun priority() = DEFAULT_PRIORITY

    fun buildType(
        builder: SchemaPropertyBuilder,
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): Optional<JsonSchemaElement>
}