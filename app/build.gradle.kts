plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.finallab.smartschoolpickupsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.finallab.smartschoolpickupsystem"
        minSdk = 21
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

    // for qr code
    implementation("com.google.zxing:core:3.4.1")
    implementation("androidmads.library.qrgenearator:QRGenearator:1.0.3")

}