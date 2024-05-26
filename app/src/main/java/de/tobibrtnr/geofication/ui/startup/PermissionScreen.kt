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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import de.tobibrtnr.geofication.R
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
    stringResource(R.string.permissions_1_h) to stringResource(R.string.permissions_1_a),
    stringResource(R.string.permissions_2_h) to stringResource(R.string.permissions_2_a),
    stringResource(R.string.permissions_3_h) to stringResource(R.string.permissions_3_a)
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
      Text(text = stringResource(R.string.welcome_to_geofication), style = MaterialTheme.typography.titleLarge)
      Text(stringResource(R.string.please_allow_all_required_permissions))
      Spacer(Modifier.height(48.dp))
      Row {
        Button(onClick = {
          MainScope().launch {
            notifState.launchPermissionRequest()
          }
        }) {
          Icon(
            imageVector = if (notifState.status.isGranted) Icons.Outlined.TaskAlt else Icons.Outlined.Circle,
            contentDescription = stringResource(R.string.permission_status)
          )
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.notification_permission))
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = {
          dialogInfoIndex = 0
        }) {
          Icon(
            imageVector = Icons.Outlined.Info, contentDescription = stringResource(R.string.show_info)
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
            contentDescription = stringResource(R.string.permission_status)
          )
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.general_location_permission))
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = {
          dialogInfoIndex = 1
        }) {
          Icon(
            imageVector = Icons.Outlined.Info, contentDescription = stringResource(R.string.show_info)
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
            contentDescription = stringResource(R.string.permission_status)
          )
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.background_location_permission))
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = {
          dialogInfoIndex = 2
        }) {
          Icon(
            imageVector = Icons.Outlined.Info, contentDescription = stringResource(R.string.show_info)
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Button(enabled = notifState.status.isGranted && locState.allPermissionsGranted && bgState.status.isGranted,
        onClick = { callback() }) {
        Text(stringResource(R.string.continue_text))
      }
    }
  }
}
