import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("java-library")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

dependencies {
  api("org.springframework.boot:spring-boot-starter-data-jpa")
  api("org.springframework.boot:spring-boot-starter-integration")
  api("org.springframework.integration:spring-integration-jdbc")

  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.security:spring-security-core")

  api("com.ibm.icu:icu4j:78.3")
}

tasks.named<BootJar>("bootJar") {
  enabled = false
}
