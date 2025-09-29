// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
}

subprojects {
    val aarPath =
        (rootProject.findProperty("atomicxcoreAarPath") as String?) ?: System.getProperty("atomicxcoreAarPath")

    if (!aarPath.isNullOrBlank()) {
        println("\n[AtomicXCore] 🚀 Starting dependency replacement in project: ${project.path}")
        println("[AtomicXCore] 📁 AAR path: $aarPath")
        println("[AtomicXCore] 🎯 Target dependency to replace: :atomic-x-core")
        println("-".repeat(60))


        configurations.configureEach {
            val configName = this.name

            withDependencies {
                val toRemove = this.filterIsInstance<ProjectDependency>()
                    .filter { it.dependencyProject.path == ":atomic-x-core" }

                if (toRemove.isNotEmpty()) {
                    println("[AtomicXCore] 🔧 Processing configuration: $configName in module: ${project.path}")
                    println("[AtomicXCore] 📦 Found ${toRemove.size} dependency(ies) to replace")

                    toRemove.forEach { dependency ->
                        println("[AtomicXCore] 🗑️  Removing project dependency:")
                        println("      From module: ${project.path}")
                        println("      Configuration: $configName")
                        println("      Dependency path: ${dependency.dependencyProject.path}")
                        remove(dependency)
                    }

                    val fileDependency = project.dependencies.create(project.files(aarPath))
                    println("[AtomicXCore] ➕ Adding AAR file dependency:")
                    println("      To module: ${project.path}")
                    println("      Configuration: $configName")
                    println("      AAR file: ${fileDependency.name}")
                    add(fileDependency)

                    println("[AtomicXCore] ✅ Successfully replaced in $configName")
                    println("")
                }
            }
        }

        println("=".repeat(60) + "\n")
    }
}
