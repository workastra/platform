plugins {
	id("org.springframework.boot") version "4.0.5" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
	group = "com.workastra"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}
