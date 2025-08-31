package de.mxci.micronaut.langchain.contrib.tools.parameter.typeprocessor

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentTypeProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import de.mxci.micronaut.langchain.contrib.tools.parameter.SchemaPropertyBuilder
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema
import dev.langchain4j.model.chat.request.json.JsonNumberSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import dev.langchain4j.model.chat.request.json.JsonStringSchema
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
@RequiresContrib
class PrimitiveTypeProcessor : ArgumentTypeProcessor {

    override fun buildType(
        builder: SchemaPropertyBuilder,
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): Optional<JsonSchemaElement> {
        val type = arg.type
        val description = builder.buildDescription(beanDefinition, method, arg, buildContext)
        val schema = when {
            CharSequence::class.java.isAssignableFrom(type) || type == String::class.java ->
                JsonStringSchema.Builder()
                    .description(description)
                    .build()
            type == java.lang.Boolean::class.java || type == Boolean::class.java ->
                JsonBooleanSchema.Builder()
                    .description(description)
                    .build()
            isNumberType(type) ->
                JsonIntegerSchema.Builder()
                    .description(description)
                    .build()
            isFloatingType(type) ->
                JsonNumberSchema.Builder()
                    .description(description)
                    .build()
            else -> null
        }
        return Optional.ofNullable(schema)
    }

    private fun isNumberType(type: Class<out Any>): Boolean = type == Integer::class.java || type == Int::class.java ||
        type == java.lang.Long::class.java || type == Long::class.java ||
        type == java.lang.Short::class.java || type == Short::class.java ||
        type == java.lang.Byte::class.java || type == Byte::class.java

    private fun isFloatingType(type: Class<out Any>): Boolean =
        type == java.lang.Double::class.java || type == Double::class.java ||
            type == java.lang.Float::class.java || type == Float::class.java ||
            Number::class.java.isAssignableFrom(type)
}