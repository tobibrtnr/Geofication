// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("com.android.application") version "9.1.0" apply false
  id("org.jetbrains.kotlin.android") version "2.2.10" apply false
  id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
  id("com.google.devtools.ksp") version "2.3.2" apply false
}

buildscript {
  dependencies {
    classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    classpath("com.google.android.gms:oss-licenses-plugin:0.10.7")
  }
}
