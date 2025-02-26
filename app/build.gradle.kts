plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.chakrawellness.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chakrawellness.app"
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose.v1100)
    implementation(libs.material3)
    implementation(libs.androidx.navigation.compose)


    // Firebase Authentication
    implementation(libs.google.firebase.auth.ktx)

    // Google Play Services Auth
    implementation(libs.play.services.auth)

    implementation (libs.ui)

    implementation(libs.material)

    implementation(libs.androidx.core.ktx.v1101)
    implementation(libs.androidx.activity.compose.v172)
    implementation(libs.androidx.material3.v111)
    implementation(libs.androidx.navigation.compose.v260)

    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.material:material:1.5.1")
    implementation("androidx.compose.foundation:foundation:1.5.1")

    implementation("androidx.compose.material3:material3:1.2.0")


    implementation(libs.coil.compose)

    implementation (libs.play.services.auth.v2050) // For Google Sign-In
    implementation (libs.com.google.firebase.firebase.auth.ktx) // Firebase Auth

    dependencies {
        implementation(libs.play.services.auth.v2070) // Latest Google Sign-In SDK
    }





}