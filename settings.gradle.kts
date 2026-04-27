rootProject.name = "workastra.platform"

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    "core",
    "console",
    "iam",
    "migration"
)
