package de.mxci.micronaut.langchain.contrib.tools.parameter

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import dev.langchain4j.model.chat.request.json.JsonReferenceSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class DefaultSchemaPropertyBuilder(
    argumentTypeProcessors: List<ArgumentTypeProcessor>,
    private val argumentDescriptionProcessors: List<ArgumentDescriptionProcessor>,
    private val objectMapper: ObjectMapper,
) : SchemaPropertyBuilder {

    private val orderedArgumentTypeProcessors = argumentTypeProcessors.sortedBy { it.priority() }

    override fun build(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): JsonSchemaElement {
        val typeRef = buildContext.typeRef(arg)
        return if (typeRef != null) {
            JsonReferenceSchema.builder()
                .reference(typeRef)
                .build()
        } else {
            buildTypeInternal(beanDefinition, method, arg, buildContext)
        }
    }

    override fun buildDescription(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): String = objectMapper.writeValueAsString(
        argumentDescriptionProcessors
            .flatMap { it.process(beanDefinition, method, arg, buildContext) }
            .associate { it.facet to it.value }
    )

    private fun buildTypeInternal(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): JsonSchemaElement {
        for (processor in orderedArgumentTypeProcessors) {
            val result = processor.buildType(
                this,
                beanDefinition,
                method,
                arg,
                buildContext,
            )
            if (result.isPresent) {
                return result.get()
            }
        }
        throw IllegalArgumentException("No processor found for type ${arg.type}")
    }
}