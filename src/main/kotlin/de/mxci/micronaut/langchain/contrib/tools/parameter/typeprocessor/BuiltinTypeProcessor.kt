package de.mxci.micronaut.langchain.contrib.tools.parameter.typeprocessor

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentTypeProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import de.mxci.micronaut.langchain.contrib.tools.parameter.SchemaPropertyBuilder
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import dev.langchain4j.model.chat.request.json.JsonStringSchema
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
@RequiresContrib
class BuiltinTypeProcessor : ArgumentTypeProcessor {
    override fun buildType(
        builder: SchemaPropertyBuilder,
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): Optional<JsonSchemaElement> {
        // TODO: some of these probably should trigger some additional type hints in the description
        val schema = when (arg.type) {
            java.util.UUID::class.java, java.net.URI::class.java, java.net.URL::class.java,
            java.time.LocalDate::class.java, java.time.LocalTime::class.java, java.time.Instant::class.java,
            java.time.OffsetDateTime::class.java,
            java.time.ZonedDateTime::class.java,
            java.time.LocalDateTime::class.java, java.time.Duration::class.java, java.time.Period::class.java,
            java.net.URI::class.java, java.net.URL::class.java, java.time.LocalDate::class.java,
            java.time.LocalTime::class.java,
            java.time.Instant::class.java,
            java.time.OffsetDateTime::class.java,
            java.time.ZonedDateTime::class.java,
            java.time.LocalDateTime::class.java,
            java.time.Duration::class.java,
            java.time.Period::class.java,
            ->
                JsonStringSchema.Builder()
                    .description(builder.buildDescription(beanDefinition, method, arg, buildContext))
                    .build()
            else -> null
        }
        return Optional.ofNullable(schema)
    }
}