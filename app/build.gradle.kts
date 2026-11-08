import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.screenshot)
}


android {
    namespace = "it.appm.myunical"
    // Compose Preview Screenshot Testing: the README screenshots in src/screenshotTest
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
    compileSdk = 37

    defaultConfig {
        applicationId = "it.appm.myunical"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release key: keystore.properties (local, ignored by git) or the CI environment variables
    val keystoreProperties = Properties().apply {
        rootProject.file("keystore.properties").takeIf { it.exists() }?.reader()?.use(::load)
    }
    fun signingValue(property: String, environment: String): String? =
        keystoreProperties.getProperty(property) ?: System.getenv(environment)
    val releaseKeyFile = signingValue("storeFile", "RELEASE_KEYSTORE_FILE")

    signingConfigs {
        if (releaseKeyFile != null) {
            create("release") {
                storeFile = file(releaseKeyFile)
                storePassword = signingValue("storePassword", "RELEASE_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "RELEASE_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Without a key the release build is signed with the debug one: fine to try it, not to publish
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    /*compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }*/
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.animation)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.serialization.converter)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.datastore.preferences.android)
    implementation(libs.androidx.material.icons.extended.android)

    implementation(libs.koin.android)
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":features:login"))
    implementation(project(":features:today"))
    implementation(project(":features:career"))
    implementation(project(":features:schedule"))
    implementation(project(":features:taxes"))
    implementation(project(":core:ui"))
    implementation(project(":features:news"))
    implementation(project(":features:directory"))
    implementation(project(":features:canteen"))

    //TEMP
    implementation(libs.koin.compose.viewmodel)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

