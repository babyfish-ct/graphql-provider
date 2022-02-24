plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")

    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:4.9.17"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-webflux-starter:4.3.1")

    api("org.babyfish.kimmer:kimmer-sql:0.2.2")
    kspTest("org.babyfish.kimmer:kimmer-ksp:0.2.2")

    implementation("com.graphql-java:graphql-java:17.3")
    implementation("org.springframework:spring-r2dbc:5.3.15")

    testImplementation(kotlin("test"))
}

ksp {
    arg("kimmer.draft", "false")
    arg("kimmer.table", "true")
    arg("kimmer.table.collection-join-only-for-sub-query", "true")
}
kotlin {
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
