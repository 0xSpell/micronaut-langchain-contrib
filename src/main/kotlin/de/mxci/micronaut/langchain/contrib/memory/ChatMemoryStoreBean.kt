package de.mxci.micronaut.langchain.contrib.memory

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore
import jakarta.inject.Singleton

@Singleton
@RequiresContrib
class ChatMemoryStoreBean : ChatMemoryStore by InMemoryChatMemoryStore()