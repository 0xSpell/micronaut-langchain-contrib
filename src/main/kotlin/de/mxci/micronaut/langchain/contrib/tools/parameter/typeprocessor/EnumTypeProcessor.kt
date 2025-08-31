package de.mxci.micronaut.langchain.contrib.tools.parameter.typeprocessor

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentTypeProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import de.mxci.micronaut.langchain.contrib.tools.parameter.SchemaPropertyBuilder
import dev.langchain4j.model.chat.request.json.JsonEnumSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
@RequiresContrib
class EnumTypeProcessor : ArgumentTypeProcessor {
    override fun buildType(
        builder: SchemaPropertyBuilder,
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): Optional<JsonSchemaElement> {
        if (!arg.type.isEnum) {
            return Optional.empty()
        }
        return Optional.of(
            JsonEnumSchema
                .Builder()
                .enumValues(arg.type.enumConstants.map { it.toString() })
                .description(builder.buildDescription(beanDefinition, method, arg, buildContext))
                .build()
        )
    }
}