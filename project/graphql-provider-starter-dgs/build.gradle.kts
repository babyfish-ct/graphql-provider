plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":graphql-provider"))
    implementation(kotlin("stdlib"))

    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:4.9.21"))
    api("org.springframework.boot:spring-boot-starter-security:2.6.6")
    api("com.netflix.graphql.dgs:graphql-dgs-webflux-starter:4.9.25")
    api("com.graphql-java:graphql-java-extended-scalars:17.0")
    compileOnly("io.jsonwebtoken:jjwt-api:0.11.2")
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
            artifactId = "graphql-provider-starter-dgs"
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
