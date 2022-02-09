plugins {
    kotlin("jvm") version "1.6.10"
}

group = "org.babyfish.graphql.provider"
version = "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")

    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:4.9.17"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-webflux-starter:4.3.1")

    implementation("org.babyfish.kimmer:kimmer:0.0.4")
    implementation(project(":graphql-provider-r2dbc"))
    implementation("com.graphql-java:graphql-java:17.3")
    implementation("org.springframework:spring-r2dbc:5.3.15")

    testImplementation(kotlin("test"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}