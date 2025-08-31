package de.mxci.micronaut.langchain.contrib.runtime

import com.fasterxml.jackson.databind.JsonNode
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.tools.ToolEntry
import de.mxci.micronaut.langchain.contrib.tools.ToolRegistry
import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.service.tool.ToolExecutor
import dev.langchain4j.service.tool.ToolProvider
import dev.langchain4j.service.tool.ToolProviderRequest
import dev.langchain4j.service.tool.ToolProviderResult
import io.micronaut.context.BeanContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.security.utils.SecurityService
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure

@Singleton
@RequiresContrib
class RegistryToolProvider(
    private val registry: ToolRegistry,
    private val securityService: SecurityService,
    private val injectedResolvers: List<InjectedArgumentResolver>,
    private val transactionHelper: RegistryTransactionHelper,
) : ToolProvider {

    @Inject
    lateinit var beanContext: BeanContext

    @Inject
    lateinit var mapper: ObjectMapper

    @Inject
    lateinit var conversionService: ConversionService

    private val log = LoggerFactory.getLogger(javaClass)

    override fun provideTools(request: ToolProviderRequest?): ToolProviderResult {
        val specs = registry.all().map { it.spec }

        val executor = ToolExecutor { toolExecutionRequest: ToolExecutionRequest, _ ->
            val name = toolExecutionRequest.name()
            val argsNode: JsonNode = mapper.readValue(toolExecutionRequest.arguments(), JsonNode::class.java)
            val entry = registry.get(name) ?: error("Tool not found: $name")

            val ctx = ToolInvocationContext(
                authentication = securityService.authentication.orElse(null)
            )

            transactionHelper.runTransactional {
                runCatching {
                    val result = invoke(entry, argsNode, ctx)
                    // LC4J accepts Any?; return strings/JSON if you want
                    result?.toString() ?: ""
                }
                    .getOrElse { "while trying to invoke the tool, an error was captured: ${it.message}" }
            }
        }

        return ToolProviderResult.builder()
            .addAll(specs.associateWith { executor })
            .build()
    }

    @Suppress("UNCHECKED_CAST")
    private fun invoke(entry: ToolEntry, argsNode: JsonNode, ctx: ToolInvocationContext): Any? {
        val bean = beanContext.getBean(entry.beanDefinition)

        val params: Array<Any?> = entry.method.arguments.filterNot { it.type.name == "kotlin.coroutines.Continuation" }
            .map { arg ->
                // 1) injected?
                injectedResolvers.firstNotNullOfOrNull { r ->
                    if (r.supports(arg)) r.resolve(arg, ctx) else null
                } ?: run {
                    println("looking for arg ${arg.name} in $argsNode")
                    // 2) JSON arg present?
                    val node = argsNode.get(arg.name)
                    if (node == null || node.isNull) {
                        null
                    } else {
                        extractFromRoot(node, arg)
                    }
                }
            }.toTypedArray()
        val hasContinuation = entry.method.arguments.any { it.type.name == "kotlin.coroutines.Continuation" }

        log.debug("Executing tool {} with args {}", entry.spec.name(), params.toList())

        if (hasContinuation) {
            return invokeSuspend(bean, entry, params)
        }

        @Suppress("SpreadOperator")
        return (entry.method as io.micronaut.inject.ExecutableMethod<Any, Any?>)
            .invoke(bean as Any, *params)
    }

    private fun extractFromRoot(node: JsonNode, arg: Argument<*>): Any? {
        return if (node.isTextual) {
            if (conversionService.canConvert(String::class.java, arg.type)) {
                conversionService.convert(node.textValue(), arg).getOrNull()
            } else {
                mapper.readValue(node.textValue(), arg)
            }
        } else {
            mapper.readValue(mapper.writeValueAsString(node), arg)
        }
    }

    private fun invokeSuspend(bean: Any, entry: ToolEntry, params: Array<Any?>): Any? = runBlocking {
        val kClass = bean::class
        // Match by name + erased param types (excluding receiver)
        val wantedTypes = entry.method.arguments
            .filterNot { it.type.name == "kotlin.coroutines.Continuation" }
            .map { it.type }

        val kfun: KFunction<*> = kClass.functions.firstOrNull { f ->
            f.name == entry.method.methodName &&
                f.parameters.size == wantedTypes.size + 1 &&
                f.parameters.drop(1).map { it.type.jvmErasure.java } == wantedTypes
        } ?: error(
            "Suspend KFunction not found for ${entry.method.methodName}(${wantedTypes.joinToString { it.simpleName }})"
        )

        // callSuspend expects receiver + params
        val argsWithReceiver = arrayOfNulls<Any?>(1 + params.size)
        argsWithReceiver[0] = bean
        System.arraycopy(params, 0, argsWithReceiver, 1, params.size)

        println("calling with type:")
        argsWithReceiver.forEach { println(it?.javaClass?.name) }
        argsWithReceiver.forEach { println(it) }
        @Suppress("UNCHECKED_CAST", "SpreadOperator")
        (kfun as KFunction<Any?>).callSuspend(*argsWithReceiver)
    }
}
