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

// A popup to confirm that the user wants to
// delete a Geofication, even if it is active.
@Composable
fun DeleteConfirmPopup(onConfirm: () -> Unit, onCancel: () -> Unit) {
  AlertDialog(
    icon = {
      Icon(
        Icons.AutoMirrored.Outlined.HelpOutline,
        contentDescription = stringResource(R.string.question_mark_icon)
      )
    },
    title = {
      Text(text = stringResource(R.string.geofication_is_active))
    },
    text = {
      Text(text = stringResource(R.string.geofication_still_active))
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
        Text(stringResource(R.string.delete))
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
