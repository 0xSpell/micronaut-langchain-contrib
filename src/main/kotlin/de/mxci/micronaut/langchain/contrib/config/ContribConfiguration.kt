package de.mxci.micronaut.langchain.contrib.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable

@ConfigurationProperties("$CONFIG_BASE.${ContribConfiguration.CONFIG_KEY}")
interface ContribConfiguration {

    companion object {
        const val CONFIG_KEY = "config"
    }

    @get:Bindable(defaultValue = "true")
    val enabled: Boolean

    @get:Bindable(defaultValue = "anthropic")
    val provider: String

    @get:Bindable
    val apiKey: String?

    @get:Bindable(defaultValue = "claude-sonnet-4-20250514")
    val modelName: String

    @get:Bindable(defaultValue = "30")
    val maxMessages: Int
}