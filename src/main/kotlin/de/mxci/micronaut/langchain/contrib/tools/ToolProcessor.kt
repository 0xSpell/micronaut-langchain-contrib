package de.mxci.micronaut.langchain.contrib.tools

import de.mxci.micronaut.langchain.contrib.annotation.AiTool
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import de.mxci.micronaut.langchain.contrib.tools.parameter.SchemaPropertyBuilder
import dev.langchain4j.agent.tool.ToolSpecification
import io.micronaut.context.annotation.Context
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Context
@Singleton
@RequiresContrib
class ToolProcessor(
    private val registry: ToolRegistry,
    private val toolDescriptionBuilders: List<ToolDescriptionBuilder>,
    private val toolParameterBuilder: ToolParameterBuilder,
    private val propertyBuilder: SchemaPropertyBuilder,
    private val objectMapper: ObjectMapper,
) : ExecutableMethodProcessor<AiTool> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(beanDefinition: BeanDefinition<*>?, method: ExecutableMethod<*, *>?) {
        runCatching {
            requireNotNull(beanDefinition)
            requireNotNull(method)
            if (!method.isAnnotationPresent(AiTool::class.java)) {
                log.trace("Skipping invalid method {} in {}", method.name, beanDefinition.beanType)
                return
            }
            val name = when (
                val n = method.stringValue(AiTool::class.java, "value")
                    .orElse(AiTool.ELEMENT_NAME)
            ) {
                AiTool.ELEMENT_NAME -> method.name
                else -> n
            }
            val ctx = ParamBuildContext(propertyBuilder)
            val spec = ToolSpecification.builder()
                .name(name)
                .description(
                    objectMapper.writeValueAsString(
                        toolDescriptionBuilders
                            .flatMap { it.build(beanDefinition, method, ctx) }
                            .associate { it.facet to it.value }
                    )

                )
                .parameters(toolParameterBuilder.build(beanDefinition, method, ctx))
                .build()
            registry.register(
                ToolEntry(
                    spec = spec,
                    beanDefinition = beanDefinition,
                    method = method
                )
            )
        }.onFailure {
            log.error("Error while processing method ${method?.name} in ${beanDefinition?.beanType}", it)
        }
    }
}