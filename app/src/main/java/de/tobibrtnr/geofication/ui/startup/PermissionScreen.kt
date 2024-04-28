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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
  var dialogInfoIndex by remember { mutableStateOf(-1) }

  val infoDialogContent: List<Pair<String, String>> = listOf(
    "Notifications" to "The notification permission is needed in order to send you a message when you enter or exit your specified geofence.",
    "General Location" to "The general location permission allows the app to track your location. This is needed to check when you enter or exit your geofences.",
    "Background Location" to "The background location permission is needed in order for the app to also work when it is closed. If another window opens on click on the button, please select \"Allow all the time\"."
  )

  if (dialogInfoIndex >= 0) {
    InfoDialog(
      title = infoDialogContent[dialogInfoIndex].first,
      text = infoDialogContent[dialogInfoIndex].second
    ) {
      dialogInfoIndex = -1
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(50.dp)
  ) {
    Column {
      Spacer(Modifier.height(32.dp))
      Text("Welcome to Geofication!", style = MaterialTheme.typography.titleLarge)
      Text("Please allow all required permissions.")
      Spacer(Modifier.height(48.dp))
      Row {
        Button(onClick = {
          MainScope().launch {
            notifState.launchPermissionRequest()
          }
        }) {
          Icon(
            imageVector = if (notifState.status.isGranted) Icons.Outlined.TaskAlt else Icons.Outlined.Circle,
            contentDescription = "Permission Status"
          )
          Spacer(Modifier.width(4.dp))
          Text("Notification permission")
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = {
          dialogInfoIndex = 0
        }) {
          Icon(
            imageVector = Icons.Outlined.Info, contentDescription = "Show info"
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
            imageVector = if (locState.allPermissionsGranted) Icons.Outlined.TaskAlt else Icons.Outlined.Circle,
            contentDescription = "Permission Status"
          )
          Spacer(Modifier.width(4.dp))
          Text("General location permission")
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = {
          dialogInfoIndex = 1
        }) {
          Icon(
            imageVector = Icons.Outlined.Info, contentDescription = "Show info"
          )
        }
      }
      Spacer(Modifier.height(8.dp))
      Row {
        Button(enabled = locState.allPermissionsGranted, onClick = {
          MainScope().launch {
            bgState.launchPermissionRequest()
          }
        }) {
          Icon(
            imageVector = if (bgState.status.isGranted) Icons.Outlined.TaskAlt else Icons.Outlined.Circle,
            contentDescription = "Permission Status"
          )
          Spacer(Modifier.width(4.dp))
          Text("Background location permission")
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = {
          dialogInfoIndex = 2
        }) {
          Icon(
            imageVector = Icons.Outlined.Info, contentDescription = "Show info"
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Button(enabled = notifState.status.isGranted && locState.allPermissionsGranted && bgState.status.isGranted,
        onClick = { callback() }) {
        Text("Continue")
      }
    }
  }
}
