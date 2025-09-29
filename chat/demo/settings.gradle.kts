pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Chat"
include(":app")

include(":atomic-x")
project(":atomic-x").projectDir = file("${settingsDir.path}/../../atomic-x")

include(":uikit")
project(":uikit").projectDir = file("${settingsDir.path}/../uikit")
