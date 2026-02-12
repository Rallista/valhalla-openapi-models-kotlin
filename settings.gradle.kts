plugins {
    kotlin("jvm") version "2.3.0" apply false
}

rootProject.name = "valhalla-models"

gradle.beforeProject {
    extensions.extraProperties["libraryVersion"] = "0.1.1"
}

include("client")
include("config")
