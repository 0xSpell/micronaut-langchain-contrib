package de.mxci.micronaut.langchain.contrib.runtime

import de.mxci.micronaut.langchain.contrib.config.RequiresContrib
import jakarta.inject.Singleton
import jakarta.transaction.Transactional

@RequiresContrib
@Singleton
@Transactional
open class RegistryTransactionHelper {
    open fun <T> runTransactional(block: () -> T): T = block()
}