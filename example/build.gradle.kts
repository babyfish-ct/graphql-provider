import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("com.google.devtools.ksp") version "1.6.10-1.0.2"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
}

group = "org.babyfish.graphql.provider"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.babyfish.kimmer:kimmer-sql:0.2.2")
	ksp("org.babyfish.kimmer:kimmer-ksp:0.2.2")
	implementation(files("/Users/chentao/projects/git/graphql-provider/project/graphql-provider/build/libs/graphql-provider-0.0.0-SNAPSHOT.jar"))
	implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:4.9.17"))
	implementation("com.netflix.graphql.dgs:graphql-dgs-webflux-starter:4.3.1")
	implementation("org.springframework.data:spring-data-r2dbc:1.4.2")
	runtimeOnly("io.r2dbc:r2dbc-h2:0.8.5.RELEASE")
}

ksp {
	arg("kimmer.draft", "false")
	arg("kimmer.table", "true")
	arg("kimmer.table.collection-join-only-for-sub-query", "true")
}
kotlin {
	sourceSets.main {
		kotlin.srcDir("build/generated/ksp/main/kotlin")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
