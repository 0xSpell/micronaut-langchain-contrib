plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.allopen)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.micronaut.library)
    alias(libs.plugins.detekt)
}

version = "0.0.1"
group = "de.mxci.micronaut"

repositories {
    mavenCentral()
}

dependencies {
    // Micronaut dependencies
    ksp("io.micronaut.langchain4j:micronaut-langchain4j-processor")
    implementation("io.micronaut.langchain4j:micronaut-langchain4j-core")
    implementation("io.micronaut.security:micronaut-security")
    implementation("dev.langchain4j:langchain4j-open-ai")
    implementation("dev.langchain4j:langchain4j-ollama")
    implementation("dev.langchain4j:langchain4j-anthropic")

    // Add other dependencies as needed
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.views:micronaut-views-core")

    // Arrow dependencies
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    // Kotlin dependencies
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.core.jvm)

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation(libs.transaction.api)

    detektPlugins(libs.detekt.formatting)
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
}

kotlin {
    jvmToolchain(21)
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("ai.iv3.micronaut.*")
    }
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    config.setFrom(file("detekt.yml"))
    this.ignoreFailures = false
    buildUponDefaultConfig = true
}