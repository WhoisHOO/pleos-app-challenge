import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing from keystore.properties at the repository root (not in git); its storeFile is relative to the
// repository root or absolute. Without it, the release APK is built unsigned and cannot be installed.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
gradle.taskGraph.whenReady {
    if (keystoreProperties.isEmpty && allTasks.any { it.name == "packageRelease" }) {
        logger.warn("keystore.properties not found: the release APK will be unsigned (app-release-unsigned.apk)")
    }
}

android {
    namespace = "com.whohoo.roadio"
    compileSdk = 35
    // Car API (CarUxRestrictions); the car provides it at run time.
    useLibrary("android.car")

    signingConfigs {
        if (!keystoreProperties.isEmpty) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.whohoo.roadio"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    // The route and its stories come from assets/route.json.
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Speech on Pleos Connect, whose cars have no standard Android TTS engine.
    implementation(libs.pleos.tts)
    // The car's position on Pleos Connect; the data artifact holds its error codes.
    implementation(libs.pleos.fused.location)
    implementation(libs.pleos.fused.location.data)
    // The car's navigation: the story route as waypoints, and the next turn while it guides.
    implementation(libs.pleos.navi.helper)
    implementation(libs.pleos.navi.data)
    // The car's own trip meter and odometer, for the summary at the end of a drive. Its data and aidl artifacts declare
    // Hilt, which none of their classes use; leaving it out keeps Dagger out of the APK.
    implementation(libs.pleos.vehicle) {
        exclude(group = "com.google.dagger")
    }
    // Media session for the car's media centre and steering-wheel keys, and to keep telling stories in the background.
    implementation(libs.androidx.media3.session)

    testImplementation(libs.junit)
}
