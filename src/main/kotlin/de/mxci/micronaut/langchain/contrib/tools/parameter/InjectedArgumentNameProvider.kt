package de.mxci.micronaut.langchain.contrib.tools.parameter

interface InjectedArgumentNameProvider {
    fun getArgumentNames(): List<String>
}