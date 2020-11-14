import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("java-library")
	id("maven")
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
}

group = "com.pedrocomitto"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_13
java.targetCompatibility = JavaVersion.VERSION_13


repositories {
	mavenLocal()
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:2.3.0.RELEASE")
	}
}

dependencies {
	api("org.springframework.boot:spring-boot-starter")
	api("org.springframework.retry:spring-retry")
	api("org.springframework:spring-aspects")

	api("org.springframework.boot:spring-boot-starter-data-redis")
	api("redis.clients:jedis:3.3.0")

	api("org.apache.commons:commons-pool2:2.9.0")

	api("org.jetbrains.kotlin:kotlin-reflect")
	api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

//tasks.withType<Test> {
//	useJUnitPlatform()
//}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "13"
	}
}

java {
	withSourcesJar()
	withJavadocJar()
}
