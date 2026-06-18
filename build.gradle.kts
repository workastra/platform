import net.ltgt.gradle.errorprone.errorprone

plugins {
  java
  checkstyle
	id("org.springframework.boot") version "4.0.6" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
  id("com.diffplug.spotless") version "8.7.0"
  id("net.ltgt.errorprone") version "5.1.0"
  id("io.freefair.lombok") version "9.5.0"
}

allprojects {
  group = "com.workastra"
  version = "0.0.1-SNAPSHOT"

  repositories {
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "checkstyle")
  apply(plugin = "com.diffplug.spotless")
  apply(plugin = "net.ltgt.errorprone")
  apply(plugin = "io.freefair.lombok")

  java {
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(26))
    }
  }

  dependencies {
    errorprone("com.uber.nullaway:nullaway:0.13.3")
    errorprone("com.google.errorprone:error_prone_core:2.49.0")
  }

  checkstyle {
    toolVersion = "13.4.0"
  }

  spotless {
    java {
      target("src/*/java/**/*.java")
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
      excludedPaths = ".*/build/generated/.*"

      errorproneArgs.addAll(listOf(
        "-Xep:NullAway:ERROR",
        "-XepOpt:NullAway:JSpecifyMode=true",
        "-XepOpt:NullAway:AnnotatedPackages=com.workastra",
      ))
    }
  }
}
