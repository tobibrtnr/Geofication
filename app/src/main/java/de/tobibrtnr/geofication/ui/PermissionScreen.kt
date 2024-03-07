package de.tobibrtnr.geofication.ui

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
  pState: MultiplePermissionsState,
  bgState: PermissionState,
  callback: () -> Unit
) {
  if (!pState.allPermissionsGranted) {
    println("NOT ALL PERMISSIONS GRANTED!")
    LaunchedEffect(Unit) {
      println("LAUNCH MULTIPLE PERMISSION REQUEST")
      pState.launchMultiplePermissionRequest()
    }
  } else {
    if (bgState.status.isGranted) {
      println("ALL PERMISSIONS GRANTED!")
      callback()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(50.dp)
  ) {
    Column {
      Text("Welcome to Geofication!\nPlease allow all required permissions.")

      Button(onClick = {
        MainScope().launch {
          println("LAUNCH MULTIPLE PERMISSION REQUEST")
          bgState.launchPermissionRequest()
        }
      }) {
        Text("Request background permission")
      }
    }
  }
}
