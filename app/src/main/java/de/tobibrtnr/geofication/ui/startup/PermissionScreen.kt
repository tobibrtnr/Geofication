package de.tobibrtnr.geofication.ui.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
  locState: MultiplePermissionsState,
  bgState: PermissionState,
  notifState: PermissionState,
  callback: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(50.dp)
  ) {
    Column {
      Text("Welcome to Geofication!\nPlease allow all required permissions.")
      Spacer(Modifier.height(8.dp))
      Row {
        Button(onClick = {
          MainScope().launch {
            notifState.launchPermissionRequest()
          }
        }) {
          Icon(
            imageVector = if(notifState.status.isGranted) Icons.Outlined.TaskAlt else Icons.Outlined.Circle,
            contentDescription = "Permission Status"
          )
          Spacer(Modifier.width(4.dp))
          Text("Notification permission")
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = { /*TODO*/ }) {
          Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Show info"
          )
        }
      }
      Spacer(Modifier.height(8.dp))
      Row {
        Button(onClick = {
          MainScope().launch {
            locState.launchMultiplePermissionRequest()
          }
        }) {
          Icon(
            imageVector = if(locState.allPermissionsGranted) Icons.Outlined.TaskAlt else Icons.Outlined.Circle,
            contentDescription = "Permission Status"
          )
          Spacer(Modifier.width(4.dp))
          Text("General location permission")
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = { /*TODO*/ }) {
          Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Show info"
          )
        }
      }
      Spacer(Modifier.height(8.dp))
      Row {
        Button(
          enabled = locState.allPermissionsGranted,
          onClick = {
          MainScope().launch {
            bgState.launchPermissionRequest()
          }
        }) {
          Icon(
            imageVector = if(bgState.status.isGranted) Icons.Outlined.TaskAlt else Icons.Outlined.Circle,
            contentDescription = "Permission Status"
          )
          Spacer(Modifier.width(4.dp))
          Text("Background location permission")
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = { /*TODO*/ }) {
          Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Show info"
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Button(
        enabled = notifState.status.isGranted && locState.allPermissionsGranted && bgState.status.isGranted,
        onClick = { callback() }
      ) {
        Text("Continue")
      }
    }
  }
}
