// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}
val compileSdkVersion by extra(35)
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}