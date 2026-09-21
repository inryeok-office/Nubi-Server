import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "com.inryeokoffice"
version = "0.0.1-SNAPSHOT"
description = "NUBI mobility support and accessibility analysis platform server"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

ktlint {
    version.set("1.5.0")
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
}

fun loadLocalDotEnv(file: File): Map<String, String> {
    if (!file.exists()) return emptyMap()

    val values = linkedMapOf<String, String>()
    file.useLines { lines ->
        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("#")) return@forEachIndexed

            val separator = line.indexOf('=')
            if (separator <= 0) {
                throw GradleException("Invalid .env entry at line ${index + 1}: expected KEY=value")
            }

            val key = line.substring(0, separator).trim()
            if (!key.matches(Regex("[A-Za-z_][A-Za-z0-9_]*"))) {
                throw GradleException("Invalid .env key at line ${index + 1}")
            }

            val rawValue = line.substring(separator + 1).trim()
            val value =
                when {
                    rawValue.length >= 2 && rawValue.first() in charArrayOf('\'', '"') -> {
                        val quote = rawValue.first()
                        if (rawValue.last() != quote) {
                            throw GradleException("Unclosed quote in .env entry at line ${index + 1}")
                        }
                        rawValue.substring(1, rawValue.length - 1)
                    }

                    else -> {
                        val commentStart = Regex("\\s+#").find(rawValue)?.range?.first
                        if (commentStart == null) rawValue else rawValue.substring(0, commentStart).trimEnd()
                    }
                }
            values[key] = value
        }
    }
    return values
}

tasks.register<JavaExec>("validateGwangjuBusOfficialApi") {
    group = "verification"
    description = "Validate the official Gwangju bus OpenAPI using the local .env without changing production configuration."
    dependsOn("testClasses")
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.inryeokoffice.nubi.validation.GwangjuBusOfficialApiValidationKt")

    doFirst {
        val values = loadLocalDotEnv(rootProject.file(".env"))
        val apiKey = values["GWANGJU_BUS_API_KEY"]?.trim()
        if (apiKey.isNullOrEmpty()) {
            throw GradleException("GWANGJU_BUS_API_KEY is missing. Add it to the local .env file.")
        }

        // Pass only the allowlisted validation inputs to the child process.
        environment("GWANGJU_BUS_API_KEY", apiKey)
        values["GWANGJU_BUS_OFFICIAL_BASE_URL"]
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { environment("GWANGJU_BUS_OFFICIAL_BASE_URL", it) }
        values["GWANGJU_BUS_OBSERVED_BASE_URL"]
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { environment("GWANGJU_BUS_OBSERVED_BASE_URL", it) }
    }
}
