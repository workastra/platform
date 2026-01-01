plugins {
	java
	id("org.springframework.boot") 
	id("io.spring.dependency-management")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-authorization-server")
  testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-authorization-server-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
