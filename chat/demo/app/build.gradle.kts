import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val appVersionCode = (findProperty("VERSION_CODE") as String?
    ?: System.getenv("VERSION_CODE"))?.toIntOrNull() ?: 1
val appVersionName = (findProperty("VERSION_NAME") as String?
    ?: System.getenv("VERSION_NAME")) ?: "1.0"

// -Parm64Only=true packages arm64-v8a only; otherwise arm64-v8a + armeabi-v7a + x86_64.
val arm64Only = providers.gradleProperty("arm64Only").map(String::toBoolean).getOrElse(false)

android {
    namespace = "io.trtc.tuikit.chat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tencent.qcloud.tim.tuikit.compose"
        minSdk = 23
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        ndk {
            abiFilters += if (arm64Only) listOf("arm64-v8a") else listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
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
    buildFeatures {
        compose = true
    }

    packaging {
        jniLibs {
            pickFirsts += listOf(
                "**/libImSDK.so",
                "**/libc++_shared.so",
                "**/libliteavsdk.so",
                "**/libtxffmpeg.so",
                "**/libtxsoundtouch.so",
                "**/libtcpcore-master.so",
                "**/libtcpdownloadproxy.so",
                "**/libtcpthirdparties-master.so",
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(project(":uikit"))
    implementation(project(":atomic_x"))
    implementation(project(":tuicallkit-kt"))

    implementation("com.tencent.imsdk:imsdk-plus:latest.release")
    implementation("com.tencent.imsdk:timquic-plugin:latest.release")
    implementation("com.tencent.liteav.tuikit:tuicore:9.0.+") {
        exclude("com.tencent.imsdk", "imsdk-plus")
    }
    implementation("com.tencentcloud.desk:aideskcustomer:latest.release")

    implementation("com.tencent:mmkv:2.4.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.foundation:foundation:1.8.0")
    implementation("androidx.compose.ui:ui:1.8.0")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.9")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("io.coil-kt.coil3:coil-gif:3.1.0")
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
}
