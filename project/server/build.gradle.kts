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

    implementation("org.springframework.boot:spring-boot-autoconfigure:2.6.2")

    implementation("org.babyfish.kimmer:kimmer:0.0.3")
    implementation(project(":graphql-provider-r2dbc"))
    implementation("com.graphql-java:graphql-java:17.3")

    testImplementation(kotlin("test"))
}