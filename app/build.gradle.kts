plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.reky0.mydex"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.reky0.mydex"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // lottie (cool animations)
    implementation("com.airbnb.android:lottie:6.6.0");

    // Gson (JSON parsing)
    implementation("com.google.code.gson:gson:2.11.0");

    // okhttp3 (for API requests)
    implementation("com.squareup.okhttp3:okhttp:4.12.0");

    // Glide (loading remote images dynamically into the activity)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // MPAndroidChart (nice and easy charts)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}