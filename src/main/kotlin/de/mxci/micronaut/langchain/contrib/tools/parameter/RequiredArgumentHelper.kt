package de.mxci.micronaut.langchain.contrib.tools.parameter

import io.micronaut.core.type.Argument
import org.jetbrains.annotations.NotNull

fun isRequired(arg: Argument<*>): Boolean {
    if (arg.isNonNull || arg.annotationMetadata.hasAnnotation(NotNull::class.java)) {
        return true
    }

    // Jackson’s @JsonProperty(required=true) if present
    return try {
        val ann = com.fasterxml.jackson.annotation.JsonProperty::class.java
        if (arg.annotationMetadata.booleanValue(ann, "required").orElse(false)) {
            true
        } else {
            !arg.isNullable
        }
    } catch (_: Throwable) {
        !arg.isNullable
    }
}