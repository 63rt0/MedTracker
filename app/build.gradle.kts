plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.berto.medtracker"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.berto.medtracker"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val copyReleaseApkToReleases by tasks.registering(org.gradle.api.tasks.Copy::class) {
    val releaseApkDir = layout.buildDirectory.dir("outputs/apk/release")

    from(releaseApkDir) {
        include("*.apk")
    }

    into(rootProject.layout.projectDirectory.dir("releases"))

    rename {
        "medtracker.apk"
    }

    onlyIf {
        val folder = releaseApkDir.get().asFile

        folder.exists() &&
                folder.listFiles { file ->
                    file.extension == "apk"
                }?.isNotEmpty() == true
    }
}

tasks.configureEach {
    if (name == "assembleRelease" || name == "packageRelease") {
        finalizedBy(copyReleaseApkToReleases)
    }
}