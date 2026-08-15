// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP 9+ compiles Kotlin for Android modules itself (no kotlin-android plugin needed);
// this pins the Kotlin compiler it uses so it matches the :battle-engine module's version.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}