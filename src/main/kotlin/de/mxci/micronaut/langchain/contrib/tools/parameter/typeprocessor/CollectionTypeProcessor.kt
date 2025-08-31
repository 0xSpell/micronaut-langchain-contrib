package de.mxci.micronaut.langchain.contrib.tools.parameter.typeprocessor

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentTypeProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import de.mxci.micronaut.langchain.contrib.tools.parameter.SchemaPropertyBuilder
import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
@RequiresContrib
class CollectionTypeProcessor : ArgumentTypeProcessor {
    override fun buildType(
        builder: SchemaPropertyBuilder,
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): Optional<JsonSchemaElement> {
        val type = arg.type
        val schema = when {
            type.isArray -> {
                val items = builder.build(beanDefinition, method, Argument.of(type.componentType), buildContext)
                JsonArraySchema.Builder()
                    .items(items)
                    .description(builder.buildDescription(beanDefinition, method, arg, buildContext))
                    .build()
            }
            java.util.Collection::class.java.isAssignableFrom(type) -> {
                val items = builder.build(beanDefinition, method, arg.typeParameters[0], buildContext)
                JsonArraySchema.Builder()
                    .items(items)
                    .description(builder.buildDescription(beanDefinition, method, arg, buildContext))
                    .build()
            }
            java.util.Map::class.java.isAssignableFrom(type) -> {
                require(arg.typeParameters[0] == String::class.java) {
                    "Map keys must be of type String for method ${method.name} in class ${beanDefinition.name} " +
                        "for arg ${arg.name}"
                }
                /* map value type is not easily expressed in json schema, so for now we just use a plain string to any
                 * schema type */
                JsonObjectSchema.Builder()
                    .description(builder.buildDescription(beanDefinition, method, arg, buildContext))
                    .build()
            }
            else -> null
        }
        return Optional.ofNullable(schema)
    }
}