
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.openapi)
}


val generatedDir = "${buildDir}/generated/openapi"

android {
    namespace = "it.appm.myunical"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.appm.myunical"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            kotlin {
                srcDir("$generatedDir/auth/src/main/kotlin")
            }
        }
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
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.serialization.converter)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.room.core)
    implementation(libs.androidx.compose.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.androidx.datastore.preferences.android)
    implementation(libs.androidx.material.icons.extended.android)



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateAuth") {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/specs/frk-authApiV1.yaml")
    outputDir.set("$generatedDir/auth")
    apiPackage.set("it.unical.esse3.auth.api")
    modelPackage.set("it.unical.esse3.auth.model")
    additionalProperties.set(mapOf(
        "serializationLibrary" to "kotlinx_serialization",
        "useCoroutines" to "true"
    ))
}

