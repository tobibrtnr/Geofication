package de.tobibrtnr.geofication.ui.startup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.infos.openLink
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
  val context = LocalContext.current

  var dialogInfoIndex by remember { mutableIntStateOf(-1) }

  // Information texts for the needed permissions.
  val infoDialogContent: List<Pair<String, String>> = listOf(
    stringResource(R.string.permissions_1_h) to stringResource(R.string.permissions_1_a),
    stringResource(R.string.permissions_2_h) to stringResource(R.string.permissions_2_a),
    stringResource(R.string.permissions_3_h) to stringResource(R.string.permissions_3_a)
  )

  // Display a requested information dialog
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
      .background(
        brush = Brush.verticalGradient(
          0.0f to MaterialTheme.colorScheme.surface,
          0.6f to MaterialTheme.colorScheme.surface,
          1f to MaterialTheme.colorScheme.primary
        )
      )
      .padding(top = 50.dp, start = 24.dp, end = 24.dp)
  ) {
    Column(
      Modifier.fillMaxHeight(),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row {
        Column {
          Spacer(Modifier.height(32.dp))
          Text(
            text = stringResource(R.string.welcome_to_geofication),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = stringResource(R.string.please_allow_all_required_permissions),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(Modifier.height(48.dp))
          // Permission requests for notifications.
          Row {
            Button(onClick = {
              MainScope().launch {
                notifState.launchPermissionRequest()
              }
            }, modifier = Modifier.weight(1f)) {
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
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(R.string.show_info)
              )
            }
          }
          Spacer(Modifier.height(8.dp))
          // Permission request for coarse and fine location
          Row {
            Button(onClick = {
              MainScope().launch {
                locState.launchMultiplePermissionRequest()
              }
            }, modifier = Modifier.weight(1f)) {
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
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(R.string.show_info)
              )
            }
          }
          Spacer(Modifier.height(8.dp))
          // Permission request for background location
          Row {
            Button(enabled = locState.allPermissionsGranted, onClick = {
              MainScope().launch {
                bgState.launchPermissionRequest()
              }
            }, modifier = Modifier.weight(1f)) {
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
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(R.string.show_info)
              )
            }
          }
          Spacer(Modifier.height(16.dp))
          // Continue to the main screen if all permissions have been granted
          Button(
            enabled = notifState.status.isGranted && locState.allPermissionsGranted && bgState.status.isGranted,
            onClick = { callback() },
            modifier = Modifier.fillMaxWidth(0.4f)
          ) {
            Text(stringResource(R.string.continue_text))
          }
        }
      }

      // Footer with information and privacy policy
      Row(
        Modifier
          .padding(WindowInsets.navigationBars.asPaddingValues())
          .fillMaxWidth()
      ) {
        Column {
          Text(
            text = stringResource(R.string.location_processing),
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
          )
          Row(
            modifier =Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = stringResource(R.string.privacy_policy),
              color = MaterialTheme.colorScheme.onPrimary,
              textAlign = TextAlign.Center,
              textDecoration = TextDecoration.Underline,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier
                .clickable {
                  openLink("https://geofication.tobibrtnr.de/privacy", context)
                }
            )
          }
        }
      }
    }
  }
}
