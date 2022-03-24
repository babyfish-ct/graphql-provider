plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    id("org.jetbrains.dokka") version "1.6.10"
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

    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:4.9.21"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-webflux-starter:4.9.21")
    implementation("com.graphql-java:graphql-java-extended-scalars:17.0")

    api("org.babyfish.kimmer:kimmer-sql:0.2.12")
    kspTest("org.babyfish.kimmer:kimmer-ksp:0.2.12")

    implementation("com.graphql-java:graphql-java:17.3")
    implementation("org.springframework.data:spring-data-r2dbc:1.4.2")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter:2.6.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.4")
    testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.6.4")
    testRuntimeOnly("io.r2dbc:r2dbc-h2:0.8.5.RELEASE")
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

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks {
    withType(Jar::class) {
        if (archiveClassifier.get() == "javadoc") {
            dependsOn(dokkaHtml)
            from("build/dokka/html")
        }
    }
}
