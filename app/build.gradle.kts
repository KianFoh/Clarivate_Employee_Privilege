plugins {
    alias(libs.plugins.android.application)

    //Custom
    id("com.google.gms.google-services") version "4.4.2"
}

android {
    namespace = "com.example.clarivate_employee_privilege"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.clarivate_employee_privilege"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("..\\External\\debug.keystore")
        }
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
    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.leanback)
    implementation(libs.glide)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Custom Dependency
    implementation (libs.com.squareup.okhttp3.okhttp2)
    implementation (libs.gson)
    implementation (libs.credentials)
    implementation (libs.credentials.play.services.auth)
    implementation (libs.play.services.auth.v2050)
    implementation(libs.picasso) //Use for getting image from image URL
    implementation (libs.circleimageview)
    implementation (libs.socket.io.client)
    implementation (libs.cardview)
    val camerax_version = "1.0.0-beta07"
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation (libs.material.v140)
    implementation (libs.viewpager2)
}