package de.mxci.micronaut.langchain.contrib.web

import de.mxci.micronaut.langchain.contrib.config.CONFIG_BASE
import de.mxci.micronaut.langchain.contrib.config.ChatConfiguration
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.runtime.AssistantService
import de.mxci.micronaut.langchain.contrib.tools.ToolRegistry
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.cookie.Cookie
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.views.ModelAndView
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/ai/")
@Requires(property = "$CONFIG_BASE.${ChatConfiguration.CONFIG_KEY}.enabled", value = "true")
@RequiresContrib
class ChatController(
    private val assistantService: AssistantService,
    private val registry: ToolRegistry,
    private val chatConfiguration: ChatConfiguration,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Get("/chat")
    fun page(): ModelAndView<Map<String, Any>> {
        val tools = registry.all().map { it.spec.name() }
        val model = mapOf("tools" to tools)
        return ModelAndView("ai/chat.ftl", model)
    }

    @Serdeable
    data class ChatRequest(@field:NotBlank val message: String)

    @Serdeable
    data class ChatResponse(val reply: String)

    @Post("/send", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun send(
        @Body req: ChatRequest,
        @CookieValue(value = "cid", defaultValue = "") cidCookie: String?,
    ): HttpResponse<ChatResponse> {
        val cid = if (!cidCookie.isNullOrBlank()) cidCookie else java.util.UUID.randomUUID().toString()

        val cookie = if (cidCookie.isNullOrBlank()) {
            Cookie.of("cid", cid)
                .path("/")
                .httpOnly(true)
                .maxAge(java.time.Duration.ofDays(chatConfiguration.cookieLifetime))
        } else {
            null
        }

        runCatching {
            val reply = assistantService.chat(cid, req.message)
            log.info("Reply: $reply")
            return HttpResponse
                .ok(ChatResponse(reply))
                .apply {
                    if (cookie != null) {
                        cookie(cookie)
                    }
                }
        }.onFailure {
            log.error("Error in chat", it)
        }.getOrThrow()
    }
}
