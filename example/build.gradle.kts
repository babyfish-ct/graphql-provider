import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("com.google.devtools.ksp") version "1.6.10-1.0.2"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
}

group = "org.babyfish.graphql.provider"
version = "0.0.5"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.babyfish.graphql.provider:graphql-provider-starter-dgs:0.0.6")
	ksp("org.babyfish.kimmer:kimmer-ksp:0.3.1")
	runtimeOnly("io.r2dbc:r2dbc-h2:0.8.5.RELEASE")
}

ksp {
	arg("kimmer.table", "true")
	arg("kimmer.table.collection-join-only-for-sub-query", "true")
}
kotlin {
	sourceSets.main {
		kotlin.srcDir("build/generated/ksp/main/kotlin")
	}
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
