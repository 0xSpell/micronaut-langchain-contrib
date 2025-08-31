package de.mxci.micronaut.langchain.contrib.tools.parameter.typeprocessor

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentTypeProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import de.mxci.micronaut.langchain.contrib.tools.parameter.SchemaPropertyBuilder
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
@RequiresContrib
class ObjectProcessor : ArgumentTypeProcessor {

    /* low priority */
    override fun priority(): Int = ArgumentTypeProcessor.DEFAULT_PRIORITY * 2

    override fun buildType(
        builder: SchemaPropertyBuilder,
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): Optional<JsonSchemaElement> {
        return when {
            arg.type === Object::class.java -> Optional.of(
                JsonObjectSchema.builder()
                    .description(builder.buildDescription(beanDefinition, method, arg, buildContext))
                    .build()
            )
            else -> Optional.empty()
        }
    }
}