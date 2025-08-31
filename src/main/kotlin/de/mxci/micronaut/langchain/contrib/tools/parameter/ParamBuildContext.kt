package de.mxci.micronaut.langchain.contrib.tools.parameter

import dev.langchain4j.model.chat.request.json.JsonSchemaElement
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod

class ParamBuildContext(
    private val propertyBuilder: SchemaPropertyBuilder,
) {
    private data class Entry(
        val name: String,
        val types: List<Class<*>>,
        var uses: Int = 0,
        var schema: JsonSchemaElement? = null,
        val attachAlways: Boolean = false,
    )

    private val typeMap = mutableMapOf<List<Class<*>>, Entry>()

    private fun referenceKeyInternal(name: String) = "#/definitions/$name"

    private fun defName(types: List<Class<*>>) =
        types.joinToString("_") { it.simpleName.ifBlank { it.name.replace('.', '_').replace('$', '_') } }

    private fun keyFor(arg: Argument<*>) =
        listOf(arg.type) + arg.typeParameters.map { it.type }

    /** Return a JSON-Schema ref ("#/definitions/Name") and bump use count if already known. */
    fun typeRef(arg: Argument<*>) = typeRef(keyFor(arg))

    fun typeRef(types: List<Class<*>>): String? =
        typeMap[types]?.also { it.uses++ }?.let { referenceKeyInternal(it.name) }

    /** Pre-register a placeholder entry (no schema yet). */
    fun preregisterType(types: List<Class<*>>) {
        typeMap.putIfAbsent(types, Entry(name = defName(types), types = types, uses = 1, schema = null))
    }

    /**
     * Force-build a type and attach it (always emitted), returning its "#/definitions/Name" ref.
     * Useful for return types or extra models a provider wants to publish.
     */
    fun attachAndProcess(
        beanDefinition: BeanDefinition<*>,
        method: ExecutableMethod<*, *>,
        arg: Argument<*>,
    ): String {
        val key = keyFor(arg)
        typeMap[key]?.let { referenceKeyInternal(it.name) }

        // Build (this may recursively register other types)
        val schema = propertyBuilder.build(beanDefinition, method, arg, this)

        val entry = Entry(
            name = defName(key),
            types = key,
            uses = 1,
            schema = schema,
            attachAlways = true
        )
        typeMap[key] = entry
        return referenceKeyInternal(entry.name)
    }

    /** Complete a previously preregistered type with its schema body. */
    fun completeType(types: List<Class<*>>, schema: JsonSchemaElement?) {
        val e = typeMap[types] ?: error("Type $types is not registered")
        require(e.schema == null) { "Type $types already has a schema" }
        e.schema = schema
    }

    /**
     * Definitions to attach on the root: map of definition-name -> schema.
     * We emit entries that were used more than once OR explicitly attached.
     */
    fun attachedDefinitions(): Map<String, JsonSchemaElement> =
        typeMap.values
            .filter { (it.uses > 1 || it.attachAlways) && it.schema != null }
            .associate { referenceKeyInternal(it.name) to it.schema!! }
}
