plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
  id("com.google.devtools.ksp")
}

android {
  namespace = "de.tobibrtnr.geofication"
  compileSdk = 34

  defaultConfig {
    applicationId = "de.tobibrtnr.geofication"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

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
  implementation("com.google.maps.android:maps-compose:4.3.3")
  // Optionally, you can include the Compose utils library
  implementation("com.google.maps.android:maps-compose-utils:4.3.3")
  // Optionally, you can include the widgets library for ScaleBar, etc.
  implementation("com.google.maps.android:maps-compose-widgets:4.3.3")

  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
  implementation("androidx.activity:activity-compose:1.8.2")
  implementation("androidx.activity:activity-ktx:1.8.2")
  implementation(platform("androidx.compose:compose-bom:2024.04.00"))
  implementation("androidx.compose.ui:ui:1.6.5")
  implementation("androidx.compose.ui:ui-graphics:1.6.5")
  implementation("androidx.compose.ui:ui-tooling-preview:1.6.5")
  implementation("androidx.compose.material3:material3:1.2.1")
  implementation("androidx.compose.material:material-icons-extended:1.6.5")

  implementation("com.google.android.gms:play-services-maps:18.2.0")

  // await
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

  // Geofencing
  implementation("com.google.android.gms:play-services-location:21.2.0")

  // Nav - From Cupcake
  implementation("androidx.navigation:navigation-compose:2.7.7")

  implementation("com.google.android.material:material:1.11.0")

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

  testImplementation("junit:junit:4.13.2")

  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
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