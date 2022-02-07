import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    antlr
}

group = "org.babyfish.graphql.provider"
version = "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    api("io.r2dbc:r2dbc-spi:0.9.0.RELEASE")

    testImplementation(kotlin("test"))
    testImplementation("io.r2dbc:r2dbc-h2:0.9.0.M1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")

    antlr("org.antlr:antlr4:4.9.3")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated-src/antlr")
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        apiVersion = "1.6"
        languageVersion = "1.6"
        jvmTarget = "16"
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}