pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/repository/maven/liteavsdk")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/repository/maven/liteavsdk")
        mavenCentral()
    }
}

rootProject.name = "Chat"

include(":app")

include(":uikit")
project(":uikit").projectDir = file("${settingsDir.path}/../uikit")

include(":atomic_x")
project(":atomic_x").projectDir = file("${settingsDir.path}/../../atomic_x")

include(":tuicallkit-kt")
project(":tuicallkit-kt").projectDir = file("${settingsDir.path}/../../call/tuicallkit-kt")
