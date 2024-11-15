plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.finallab.smartschoolpickupsystem"
    compileSdk = 35
    buildFeatures{
        viewBinding=true
    }
    defaultConfig {
        applicationId = "com.finallab.smartschoolpickupsystem"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation ("com.google.firebase:firebase-analytics:21.0.0")
    implementation ("com.google.firebase:firebase-auth:21.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.5.0")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("com.google.android.material:material:1.6.0")


    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.material:material:1.12.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.google.zxing:core:3.4.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}

