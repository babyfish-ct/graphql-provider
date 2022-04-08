plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    id("org.jetbrains.dokka") version "1.6.10"
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    api("org.babyfish.kimmer:kimmer-sql:0.3.1")
    api("org.springframework.data:spring-data-r2dbc:1.4.3")
    api("org.springframework.security:spring-security-config:5.6.2")
    api("org.springframework.security:spring-security-web:5.6.2")
    api("org.springframework:spring-webflux:5.3.18")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")

    implementation("org.springframework.boot:spring-boot-autoconfigure:2.6.5")
    implementation("com.graphql-java:graphql-java-extended-scalars:17.0")
    implementation("com.graphql-java:graphql-java:17.3")

    kspTest("org.babyfish.kimmer:kimmer-ksp:0.3.1")
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter:2.6.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.5")
    testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.6.5")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux:2.6.5")
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

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

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

// Publish to maven-----------------------------------------------------
val NEXUS_USERNAME: String by project
val NEXUS_PASSWORD: String by project

publishing {
    repositories {
        maven {
            credentials {
                username = NEXUS_USERNAME
                password = NEXUS_PASSWORD
            }
            name = "MavenCentral"
            url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                uri("https://oss.sonatype.org/content/repositories/snapshots")
            } else {
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = "graphql-provider"
            from(components["java"])
            pom {
                name.set("graphql-provider")
                description.set("SQL DSL base on kotlin")
                url.set("https://github.com/babyfish-ct/graphql-provider")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/babyfish-ct/graphql-provider/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("babyfish-ct")
                        name.set("陈涛")
                        email.set("babyfish.ct@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/babyfish-ct/graphql-provider.git")
                    developerConnection.set("scm:git:ssh://github.com/babyfish-ct/graphql-provider.git")
                    url.set("https://github.com/babyfish-ct/graphql-provider")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
