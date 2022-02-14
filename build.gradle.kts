import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
	groovy
}

group = "dev.jjrz"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb:2.6.3")
	implementation("org.springframework.retry:spring-retry:1.3.1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	runtimeOnly("org.springframework.boot:spring-boot-starter-aop")
	testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.3")
	testImplementation("org.codehaus.groovy:groovy-all:2.4.15")
	testImplementation("org.spockframework:spock-core:1.2-groovy-2.4")
	testImplementation("org.spockframework:spock-spring:1.2-groovy-2.4")
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}
