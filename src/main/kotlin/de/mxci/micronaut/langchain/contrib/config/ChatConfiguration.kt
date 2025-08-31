package de.mxci.micronaut.langchain.contrib.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable

@ConfigurationProperties("$CONFIG_BASE.${ChatConfiguration.CONFIG_KEY}")
interface ChatConfiguration {

    companion object {
        const val CONFIG_KEY = "chat"
    }

    @get:Bindable(defaultValue = "7")
    val cookieLifetime: Long

    @get:Bindable(defaultValue = "true")
    val enabled: Boolean
}