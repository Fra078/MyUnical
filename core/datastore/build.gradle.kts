plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "it.fra.myunical.datastore"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.preferences.android)
    implementation(libs.kotlin.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}