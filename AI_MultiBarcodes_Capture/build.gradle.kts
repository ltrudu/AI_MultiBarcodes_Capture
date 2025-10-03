plugins {
    alias(libs.plugins.android.application)
    
}

android {
    namespace = "com.zebra.ai_multibarcodes_capture"
    compileSdk = 35
    androidResources {
        noCompress.add("tar")
        noCompress.add("tar.crypt")
    }
    defaultConfig {
        applicationId = "com.zebra.ai_multibarcodes_capture.dev"
        minSdk = 34
        targetSdk = 35
        versionCode = 30
        versionName = "1.30"

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
    buildFeatures {
        viewBinding = true
        buildConfig = true
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

    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.androidx.camera.extensions)

    implementation(libs.json)
    implementation(libs.gson)

    //Below dependency is to get AI Suite SDK
    implementation(libs.zebra.ai.vision.sdk) { artifact { type = "aar" } }

    //Below dependency is to get Barcode Localizer model for AI Suite SDK
    implementation(libs.barcode.localizer) { artifact { type = "aar" } }

    // Dependency for CriticalPermissionHelper
    implementation(libs.criticalpermissionhelper)

    // Dependency to use internal scanner for endpoint configuration
    implementation(libs.datawedgeintentwrapper)

    // For Excel Export
    implementation(libs.poi)
    implementation(libs.poiooxml)
}