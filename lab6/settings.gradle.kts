plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "lab6"
include("src:main:kotlin")
findProject(":src:main:kotlin")?.name = "kotlin"
