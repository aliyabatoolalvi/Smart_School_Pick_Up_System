import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}
val props = Properties().apply {
    val file = rootProject.file("apikey.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

val mailjetKey = props["MAILJET_API_KEY"] as String
val mailjetSecret = props["MAILJET_SECRET_KEY"] as String

android {
    namespace = "com.finallab.smartschoolpickupsystem"
    compileSdk = 35
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.finallab.smartschoolpickupsystem"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MAILJET_API_KEY", mailjetKey)
        buildConfigField("String", "MAILJET_SECRET_KEY", mailjetSecret)

    }

    secrets {
        defaultPropertiesFileName = "secrets.properties"
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
    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.txt",
                "META-INF/LICENSE.txt",
                "META-INF/DEPENDENCIES"
            )
        }
    }

}

dependencies {
    // --- AndroidX & Core ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation("androidx.gridlayout:gridlayout:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // --- Firebase (BoM recommended) ---
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation(libs.firebase.appcheck.debug)

    // --- Google & Maps ---
    implementation(libs.play.services.maps)
    implementation("com.google.android.gms:play-services-base:18.2.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // --- UI / Graphics ---
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // --- Camera & QR ---
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.4.1")

    // --- Network & JSON ---
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.json:json:20230227")

    // --- Mail & Cloud Storage ---
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation("com.cloudinary:cloudinary-android:2.1.0")
    implementation("com.mailjet:mailjet-client:5.2.3")

    // --- Google Auth (⚠ For server-side use only!) ---
    implementation("com.google.auth:google-auth-library-oauth2-http:1.17.0")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
