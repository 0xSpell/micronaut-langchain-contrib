package de.mxci.micronaut.langchain.contrib.tools.doc

import io.micronaut.serde.annotation.Serdeable

/**
 * A facet of a description.
 * Multiple facets make up a complete description of a thing, providing different views and levels of detail.
 * Since this is an input to prompting, the typing is static.
 * Description Providers need to provide the used facets and description so that they can be assembled into the
 * global prompt.
 */
@Serdeable
data class DescriptionFacet(
    val value: String,
    val facet: String,
)
