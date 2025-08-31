package de.mxci.micronaut.langchain.contrib.runtime

import de.mxci.micronaut.langchain.contrib.config.ContribConfiguration
import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import de.mxci.micronaut.langchain.contrib.memory.ChatMemoryStoreBean
import de.mxci.micronaut.langchain.contrib.prompt.SystemMessageAssembler
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

interface AssistantService {
    fun chat(
        @MemoryId conversationId: String,
        @UserMessage userMessage: String,
    ): String
}

@RequiresContrib
@Factory
class LlmFactory(
    private val registryToolProvider: RegistryToolProvider,
    private val chatModel: ChatModel,
    private val store: ChatMemoryStoreBean,
    private val systemMessageAssembler: SystemMessageAssembler,
    private val config: ContribConfiguration,
) {
    @Singleton
    fun create() = AiServices
        .builder(AssistantService::class.java)
        .chatModel(chatModel)
        .chatMemoryProvider { id ->
            MessageWindowChatMemory.builder()
                .id(id.toString())
                .maxMessages(config.maxMessages)
                .chatMemoryStore(store)
                .build()
        }
        .toolProvider(registryToolProvider)
        .systemMessageProvider { systemMessageAssembler.buildSystemMessage(it) }
        .build()
}