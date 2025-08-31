package de.mxci.micronaut.langchain.contrib.tools.parameter

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.ToolParameterBuilder
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@RequiresContrib
class DefaultToolParameterBuilder(
    private val injectedArgumentDetectors: List<DefaultInjectedArgumentDetector>,
    private val schemaPropertyBuilder: SchemaPropertyBuilder,
) : ToolParameterBuilder {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun build(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        buildContext: ParamBuildContext,
    ): JsonObjectSchema {
        val builder = JsonObjectSchema.builder()
            .description("")
            .additionalProperties(false)
        val requiredArgs = mutableListOf<String>()

        for (arg in method.arguments) {
            if (injectedArgumentDetectors.any { it.isInjected(arg) }) {
                log.trace("Rejected arg {} in method {}: it seems to be auto-injected", arg.name, method.name)
                continue
            }
            builder.addProperty(
                arg.name,
                schemaPropertyBuilder.build(beanDefinition, method, arg, buildContext)
            )

            if (isRequired(arg)) {
                requiredArgs.add(arg.name)
            }
        }
        builder.required(requiredArgs)
        builder.definitions(buildContext.attachedDefinitions().toMap())
        return builder.build()
    }
}