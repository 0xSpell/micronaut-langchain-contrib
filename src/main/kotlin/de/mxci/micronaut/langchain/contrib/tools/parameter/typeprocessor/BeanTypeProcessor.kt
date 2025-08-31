package de.mxci.micronaut.langchain.contrib.tools.parameter.typeprocessor

import com.fasterxml.jackson.annotation.JsonProperty
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.parameter.ArgumentTypeProcessor
import de.mxci.micronaut.langchain.contrib.tools.parameter.ParamBuildContext
import de.mxci.micronaut.langchain.contrib.tools.parameter.SchemaPropertyBuilder
import de.mxci.micronaut.langchain.contrib.tools.parameter.isRequired
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import io.micronaut.core.beans.BeanIntrospection
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import java.util.*

@Singleton
@RequiresContrib
class BeanTypeProcessor : ArgumentTypeProcessor {

    override fun buildType(
        builder: SchemaPropertyBuilder,
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
        buildContext: ParamBuildContext,
    ): Optional<JsonSchemaElement> {
        val optionalIntrospection = optionalIntrospection(arg.type)
        if (!optionalIntrospection.isPresent) return Optional.empty()

        val typeKey: List<Class<*>> = listOf(arg.type) + arg.typeParameters.map { it.type }
        /* if we had already seen this type, we wouldn't be here */
        buildContext.preregisterType(typeKey)

        val introspection = optionalIntrospection.get()
        val obj = JsonObjectSchema.Builder()
            .additionalProperties(false)
            .description(builder.buildDescription(beanDefinition, method, arg, buildContext))

        val requiredArgs = mutableListOf<String>()

        for (prop: Argument<*> in introspection.constructorArguments) {
            val propArg = resolvePropertyArgument(prop, arg)
            val propSchema = builder.build(beanDefinition, method, propArg, buildContext)
            val name = prop.findAnnotation(JsonProperty::class.java)
                .flatMap { it.stringValue("value") }
                .orElseGet { prop.name }
            obj.addProperty(name, propSchema)
            if (isRequired(prop)) {
                requiredArgs.add(name)
            }
        }

        obj.required(requiredArgs)
        val body = obj.build()

        buildContext.completeType(typeKey, body)
        return Optional.of(body)
    }

    /**
     * Best-effort generic resolution:
     * - If Micronaut already resolved the property type (has concrete class or its own type params), keep it.
     * - If the property is 'Object' and the owner has exactly one type variable, substitute that.
     *   (Good enough for Example<T>(a:T) used as Example<Array<String>>)
     */
    private fun resolvePropertyArgument(propertyArg: Argument<*>, owner: Argument<*>): Argument<*> {
        // TODO: check later how this fares for Example<T>(val list: List<T>)
        if (propertyArg.type != Any::class.java || propertyArg.typeParameters.isNotEmpty()) {
            return propertyArg
        }
        val ownerTps = owner.typeParameters.toList()
        return if (ownerTps.size == 1) ownerTps[0] else propertyArg
    }

    private fun optionalIntrospection(type: Class<*>): Optional<BeanIntrospection<*>> {
        return Optional.ofNullable(
            runCatching {
                BeanIntrospection.getIntrospection(type)
            }.getOrNull()
        )
    }
}