// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("com.android.application") version "8.1.2" apply false
  id("org.jetbrains.kotlin.android") version "1.8.10" apply false
  id("com.google.devtools.ksp") version "1.9.0-1.0.12" apply false
}

buildscript {
  dependencies {
    classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:5.1.0.4882")
  }
}
