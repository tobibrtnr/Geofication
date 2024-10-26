plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
  id("com.google.devtools.ksp")
  id("com.google.android.gms.oss-licenses-plugin")
}

android {
  namespace = "de.tobibrtnr.geofication"
  compileSdk = 34

  val appVersionName = "1.0"

  defaultConfig {
    applicationId = "de.tobibrtnr.geofication"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = appVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
  implementation("com.google.maps.android:maps-compose:5.0.1")
  // Optionally, you can include the Compose utils library
  implementation("com.google.maps.android:maps-compose-utils:5.0.1")
  // Optionally, you can include the widgets library for ScaleBar, etc.
  implementation("com.google.maps.android:maps-compose-widgets:5.0.1")

  // lifecycle aware view model
  // https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state#4
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.activity:activity-ktx:1.9.3")
  implementation(platform("androidx.compose:compose-bom:2024.04.00"))
  implementation("androidx.compose.ui:ui:1.7.4")
  implementation("androidx.compose.ui:ui-graphics:1.7.4")
  implementation("androidx.compose.ui:ui-tooling-preview:1.7.4")
  implementation("androidx.compose.material3:material3:1.3.0")
  implementation("androidx.compose.material:material-icons-extended:1.7.4")

  implementation("com.google.android.gms:play-services-maps:19.0.0")

  // await
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

  // Geofencing
  implementation("com.google.android.gms:play-services-location:21.3.0")

  // Nav - From Cupcake
  implementation("androidx.navigation:navigation-compose:2.8.3")

  implementation("com.google.android.material:material:1.12.0")

  // Room - Data Storage
  val roomVersion = "2.6.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  annotationProcessor("androidx.room:room-compiler:$roomVersion")
  // To use Kotlin Symbol Processing (KSP)
  ksp("androidx.room:room-compiler:$roomVersion")
  // optional - Kotlin Extensions and Coroutines support for Room
  implementation("androidx.room:room-ktx:$roomVersion")

  // Accompanist, utilities for e.g. permission handling
  implementation("com.google.accompanist:accompanist-permissions:0.34.0")

  // WorkManager for Geofication trigger handling after x minutes
  implementation("androidx.work:work-runtime-ktx:2.9.1")

  // In-app Play Store rating
  implementation("com.google.android.play:review:2.0.2")
  implementation("com.google.android.play:review-ktx:2.0.2")

  // Display OSS licenses
  implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

  testImplementation("junit:junit:4.13.2")

  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}

secrets {
  // To add your Maps API key to this project:
  // 1. Add this line to your local.properties file, where YOUR_API_KEY is your API key:
  //        MAPS_API_KEY=YOUR_API_KEY
  defaultPropertiesFileName = "local.properties"
}