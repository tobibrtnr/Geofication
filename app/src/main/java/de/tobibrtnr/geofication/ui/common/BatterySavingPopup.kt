package de.tobibrtnr.geofication.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tobibrtnr.geofication.R

// A popup that displays a warning that Geofications may be
// inaccurate, if the battery saver mode is activated.
@Composable
fun BatterySavingPopup(onConfirm: () -> Unit, onCancel: () -> Unit) {
  AlertDialog(
    icon = {
      Icon(
        Icons.Default.BatteryAlert,
        contentDescription = stringResource(R.string.battery_alert_icon)
      )
    },
    title = {
      Text(text = stringResource(R.string.battery_saver_mode_is_active))
    },
    text = {
      Text(text = stringResource(R.string.battery_saver_mode_text))
    },
    onDismissRequest = {
      onCancel()
    },
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm()
        }
      ) {
        Text(stringResource(R.string.disable_capitalized))
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onCancel()
        }
      ) {
        Text(stringResource(R.string.keep_enabled))
      }
    }
  )
}
