import java.util.Properties
import java.io.FileInputStream

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
  id("com.google.devtools.ksp")
  id("com.google.android.gms.oss-licenses-plugin")
  id("org.sonarqube") version "6.0.1.5171"
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  namespace = "de.tobibrtnr.geofication"
  compileSdk = 35

  val appVersionName = "1.0.1"

  defaultConfig {
    applicationId = "de.tobibrtnr.geofication"
    minSdk = 26
    targetSdk = 35
    versionCode = 5
    versionName = appVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  lint {
    checkReleaseBuilds = false
    //If you want to continue even if errors found use following line
    abortOnError = false
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true

      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )

      signingConfig = signingConfigs.getByName("debug")

      buildConfigField("String", "VERSION_NAME", "\"${appVersionName}\"")
      buildConfigField("Boolean", "DEBUG", "false")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.2"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation(platform("androidx.compose:compose-bom:2025.07.00"))

  // Android Kotlin core
  val coreVersion = "1.16.0"
  implementation("androidx.core:core-ktx:$coreVersion")

  // Lifecycle aware view model
  // https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state#4
  val lifecycleVersion = "2.9.2"
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

  // Navigation for Jetpack Compose
  val navigationVersion = "2.9.3"
  implementation("androidx.navigation:navigation-compose:$navigationVersion")

  // Activities for Jetpack Compose
  val activityVersion = "1.10.1"
  implementation("androidx.activity:activity-compose:$activityVersion")
  implementation("androidx.activity:activity-ktx:$activityVersion")

  // Jetpack compose libraries
  val composeVersion = "1.8.3"
  implementation("androidx.compose.ui:ui:$composeVersion")
  implementation("androidx.compose.ui:ui-graphics:$composeVersion")
  implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
  implementation("androidx.compose.material:material-icons-extended:1.7.8")
  implementation("androidx.compose.animation:animation-graphics-android:$composeVersion")

  // WorkManager for Geofication trigger handling after x minutes
  val workVersion = "2.10.3"
  implementation("androidx.work:work-runtime-ktx:$workVersion")

  // Android Material implementation
  val materialVersion = "1.12.0"
  implementation("com.google.android.material:material:$materialVersion")

  // Material 3 for Jetpack Compose
  val material3Version = "1.3.2"
  implementation("androidx.compose.material3:material3:$material3Version")

  // await
  val awaitVersion = "1.8.1"
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$awaitVersion")

  // Google Play Services for using Google Maps SDK
  val playMapsVersion = "19.2.0"
  implementation("com.google.android.gms:play-services-maps:$playMapsVersion")

  // Google Play Services for using Geofencing API
  val playLocationVersion = "21.3.0"
  implementation("com.google.android.gms:play-services-location:$playLocationVersion")

  // In-app Play Store rating
  val playReviewVersion = "2.0.2"
  implementation("com.google.android.play:review:$playReviewVersion")
  implementation("com.google.android.play:review-ktx:$playReviewVersion")

  // Google Maps SDK for Jetpack Compose
  val mapsVersion = "5.0.1"
  implementation("com.google.maps.android:maps-compose:$mapsVersion")
  implementation("com.google.maps.android:maps-compose-utils:$mapsVersion")
  implementation("com.google.maps.android:maps-compose-widgets:$mapsVersion")

  // Room - Data Storage
  val roomVersion = "2.7.2"
  implementation("androidx.room:room-ktx:$roomVersion")
  implementation("androidx.room:room-runtime:$roomVersion")

  // Accompanist, utilities for e.g. permission handling
  val accompanistVersion = "0.34.0"
  implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

  // Display OSS licenses
  val ossVersion = "17.2.1"
  implementation("com.google.android.gms:play-services-oss-licenses:$ossVersion")

  // Custom splash screen
  val splashScreenVersion = "1.0.1"
  implementation("androidx.core:core-splashscreen:$splashScreenVersion")

  // Kotlin Symbol Processor for Room library
  ksp("androidx.room:room-compiler:$roomVersion")

  // Annotations for Room library
  annotationProcessor("androidx.room:room-compiler:$roomVersion")

  // jUnit for testing
  val jUnitVersion = "4.13.2"
  testImplementation("junit:junit:$jUnitVersion")

  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
  androidTestImplementation(platform("androidx.compose:compose-bom:2025.07.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

secrets {
  // To add your Maps API key to this project:
  // 1. Add this line to your local.properties file, where YOUR_API_KEY is your API key:
  //        MAPS_API_KEY=YOUR_API_KEY
  defaultPropertiesFileName = "local.properties"
}
