package de.tobibrtnr.geofication.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tobibrtnr.geofication.R

// A popup that will be shown to confirm
// that all user settings should be reset.
@Composable
fun ResetSettingsPopup(onConfirm: () -> Unit, onCancel: () -> Unit) {
  AlertDialog(
    icon = {
      Icon(
        Icons.AutoMirrored.Outlined.HelpOutline,
        contentDescription = stringResource(R.string.question_mark_icon)
      )
    },
    title = {
      Text(text = stringResource(R.string.reset_settings))
    },
    text = {
      Text(text = stringResource(R.string.really_want_to_reset_all_user_settings))
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
        Text(stringResource(R.string.reset))
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onCancel()
        }
      ) {
        Text(stringResource(R.string.cancel))
      }
    }
  )
}