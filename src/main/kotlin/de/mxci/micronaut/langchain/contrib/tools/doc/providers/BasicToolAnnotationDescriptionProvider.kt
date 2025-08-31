package de.mxci.micronaut.langchain.contrib.tools.doc.providers

import de.mxci.micronaut.langchain.contrib.annotation.AiHint
import de.mxci.micronaut.langchain.contrib.annotation.AiTool
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.ToolDescriptionBuilder
import de.mxci.micronaut.langchain.contrib.tools.doc.DescriptionFacet
import de.mxci.micronaut.langchain.contrib.tools.doc.FacetType
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentDescriptionProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@Singleton
@RequiresContrib
class BasicToolAnnotationDescriptionProvider : ToolDescriptionBuilder, ArgumentDescriptionProcessor {

    private val purpose = FacetType(
        name = "purpose",
        description = "What this tool does and when to use it (1–2 short sentences)."
    )

    private val methodPath = FacetType(
        name = "method_path",
        description = "Codepath of this tool method"
    )

    override fun process(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet> {
        val ann = arg.getAnnotation(AiHint::class.java)
        return ann
            ?.stringValue("value")
            ?.getOrNull()
            ?.let { listOf(DescriptionFacet(it, purpose.name)) }
            ?: emptyList()
    }

    override fun facets(): List<FacetType> = listOf(purpose, methodPath)

    override fun build(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        buildContext: ParamBuildContext,
    ): List<DescriptionFacet> {
        val ann = AiTool::class.java
        val methodName = "${beanDefinition.beanType.simpleName}.${method.methodName}"
        val methodDescription = method.stringValue(ann, "description").orElse("")
        return if (methodDescription.isNullOrBlank()) {
            listOf(DescriptionFacet("Invoke $methodName", methodPath.name))
        } else {
            listOf(
                DescriptionFacet("Invoke $methodName", methodPath.name),
                DescriptionFacet(methodDescription, purpose.name)
            )
        }
    }
}