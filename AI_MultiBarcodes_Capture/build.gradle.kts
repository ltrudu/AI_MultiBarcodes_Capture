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
        versionCode = 37
        versionName = "1.37"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add dependency versions to BuildConfig for dynamic display in About screen
        buildConfigField("String", "ZEBRA_AI_VISION_SDK_VERSION", "\"${libs.versions.zebraAIVisionSdk.get()}\"")
        buildConfigField("String", "BARCODE_LOCALIZER_VERSION", "\"${libs.versions.barcodeLocalizer.get()}\"")
        buildConfigField("String", "CRITICAL_PERMISSION_HELPER_VERSION", "\"${libs.versions.criticalpermissionhelper.get()}\"")
        buildConfigField("String", "DATAWEDGE_INTENT_WRAPPER_VERSION", "\"${libs.versions.datawedgeintentwrapper.get()}\"")

        // NDK configuration for native YUV processing
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        debug {
            // Disable native debugging to prevent dual process launch
            // Set to true only when you need to debug native C++ code
            isJniDebuggable = false
            packaging {
                jniLibs {
                    keepDebugSymbols -= setOf("**/*.so")
                }
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Configure APK output naming
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val versionName = defaultConfig.versionName
            val versionCode = defaultConfig.versionCode
            val buildType = buildType.name

            // Format: AI_Multibarcode_Capture-v1.8-release.apk or AI_Multibarcode_Capture-v1.8-debug.apk
            output.outputFileName = "AI_Multibarcode_Capture-v${versionName}-${buildType}.apk"
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

    // Get files from libs for latest release
//    implementation(fileTree("libs") {
//        include("*.jar", "*.aar")
//    })

    // Below dependency is to get AI Suite SDK
    implementation(libs.zebra.ai.vision.sdk) { artifact { type = "aar" } }

    // Below dependency is to get Barcode Localizer model for AI Suite SDK
    implementation(libs.barcode.localizer) { artifact { type = "aar" } }

    // Dependency for CriticalPermissionHelper
    implementation(libs.criticalpermissionhelper)

    // Dependency to use internal scanner for endpoint configuration
    implementation(libs.datawedgeintentwrapper)

    // For Excel Export
    implementation(libs.poi)
    implementation(libs.poiooxml)
}