plugins {
    // Plugin for building Android applications
    id("com.android.application")

    // Enables Kotlin support for Android
    id("org.jetbrains.kotlin.android")

    // Uncomment if you want to integrate Firebase later
    // id("com.google.gms.google-services")
}

android {
    // Unique namespace for your app
    namespace = "com.example.mathforkids"

    // API level your app compiles against
    compileSdk = 36

    defaultConfig {
        // App ID (used in Play Store and system)
        applicationId = "com.example.mathforkids"

        // Minimum Android version your app supports (set to 26 for adaptive icons)
        minSdk = 26

        // Recommended Android version your app targets
        targetSdk = 36

        versionCode = 14
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Use support library for vector drawables
        vectorDrawables.useSupportLibrary = true

        // Cấu hình hỗ trợ TensorFlow Lite (NẾU DÙNG MÁY ẢO CŨ CÓ THỂ CẦN)
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            // Disable code shrinking and obfuscation for now
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java and Kotlin compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Enable Jetpack Compose
    buildFeatures {
        compose = true
    }

    // Configure Jetpack Compose compiler
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    // [QUAN TRỌNG] Cấu hình để không nén file AI, giúp Model đọc được
    androidResources {
        noCompress += "tflite"
    }

    // Remove unnecessary license files from packaging
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // --- Core Android & Jetpack ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // --- Jetpack Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Backend Connection (Database) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- TensorFlow Lite (AI nhận diện chữ viết) ---
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    // --- List bài học
    implementation("io.coil-kt:coil-compose:2.7.0")
}