plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    // ADDED: Google Services plugin for Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.replay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.replay"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    lint {
        disable += "UseAppTint"
        abortOnError = false
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Material Design
    implementation(libs.material)

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Fragment
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Gson for JSON parsing (for FavoriteManager)
    implementation("com.google.code.gson:gson:2.10.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // ==================== FIREBASE ====================
    // Firebase BoM (Bill of Materials) - manages all Firebase versions
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage-ktx")

    // Firebase Analytics (optional but recommended)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // OPTIONAL: Google Sign-In (if you want Google authentication)
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // OPTIONAL: Firebase UI for easier auth implementation
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    implementation("com.google.firebase:firebase-appcheck-debug:17.1.0")
    // ==================================================

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}