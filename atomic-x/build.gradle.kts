plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"

}


android {
    namespace = "io.trtc.tuikit.atomicx"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }

    fun getResDirs(): List<String> {
        val basePath = "src/main"
        val baseDir = file("src/main")
        val dirs = listOf("$basePath/res") +
                (baseDir.listFiles()?.filter { it.isDirectory && it.name.startsWith("res-") }
                    ?.map { "$basePath/${it.name}" } ?: emptyList())
        return dirs
    }

    sourceSets {
        named("main") {
            res.setSrcDirs(getResDirs())
        }
    }
}


dependencies {

    implementation("io.trtc.uikit:atomicx-core:3.4.0.1307")
    implementation("org.ahocorasick:ahocorasick:0.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt.coil3:coil-gif:3.1.0")
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.media3:media3-exoplayer:1.6.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.6.1")
    implementation("androidx.media3:media3-ui:1.6.1")
    implementation("androidx.media3:media3-ui-compose:1.6.1")
    implementation(libs.tuicore)
    implementation(libs.com.tencent.mmkv)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.material)
}