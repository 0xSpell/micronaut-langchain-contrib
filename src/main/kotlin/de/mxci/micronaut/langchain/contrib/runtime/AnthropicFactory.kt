package de.mxci.micronaut.langchain.contrib.runtime

import de.mxci.micronaut.langchain.contrib.config.CONFIG_BASE
import de.mxci.micronaut.langchain.contrib.config.ContribConfiguration
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.chat.ChatModel
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Requires(
    property = "$CONFIG_BASE.${ContribConfiguration.CONFIG_KEY}.provider",
    value = "anthropic",
)
@RequiresContrib
@Factory
class AnthropicFactory {

    companion object {
        const val DEFAULT_TEMPERATURE = 0.2
    }

    @Singleton
    fun build(
        config: ContribConfiguration,
    ): ChatModel {
        return AnthropicChatModel
            .builder()
            .apiKey(config.apiKey)
            .modelName(config.modelName)
            .temperature(DEFAULT_TEMPERATURE)
            .build()
    }
}