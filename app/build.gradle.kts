plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.smartcampuscompanion"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartcampuscompanion"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // Firebase BoM - Manages all Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase dependencies (without versions)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Standard AndroidX and Material libraries
    implementation(libs.appcompat)
    implementation(libs.material) // Your main Material library
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.bumptech.glide:glide:4.16.0")


    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // --- THIS IS THE FIX ---
    // 1. REMOVE the old, conflicting calendar libraries.
    // implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    // implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")

    // 2. ADD the new, modern, and compatible calendar library.
    implementation("com.applandeo:material-calendar-view:1.9.2")
    // --- END OF FIX ---

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
