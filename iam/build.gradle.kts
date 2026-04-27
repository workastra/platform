import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
  id("org.graalvm.buildtools.native")
}

dependencies {
  implementation(project(":core"))
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-authorization-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  runtimeOnly("org.postgresql:postgresql")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-authorization-server-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<BootJar>("bootJar") {
  manifest.attributes["Implementation-Title"] = "Workastra IAM"
}
