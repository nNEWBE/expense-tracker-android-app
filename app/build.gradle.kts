plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.expensetrackerapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.expensetrackerapp"
        minSdk = 24
        targetSdk = 36
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
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.splashscreen)
    
    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    
    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    
    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    
    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // WorkManager
    implementation(libs.work.runtime)
    implementation("com.google.guava:guava:31.1-android")
    
    // Lottie Animations
    implementation(libs.lottie)
    
    // MPAndroidChart
    implementation(libs.mpandroidchart)
    
    // Glide (Image Loading)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    
    // Biometric
    implementation(libs.biometric)
    
    // PDF & CSV Export
    implementation(libs.itextpdf)
    implementation(libs.opencsv)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}