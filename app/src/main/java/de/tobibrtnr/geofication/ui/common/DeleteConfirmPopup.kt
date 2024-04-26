package de.tobibrtnr.geofication.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteConfirmPopup(onConfirm: () -> Unit, onCancel: () -> Unit) {
  AlertDialog(
    icon = {
      Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = "Question Mark Icon")
    },
    title = {
      Text(text = "Geofication is active")
    },
    text = {
      Text(text = "This Geofication is still active. Are you sure you want to delete it?")
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
        Text("Delete")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onCancel()
        }
      ) {
        Text("Cancel")
      }
    }
  )
}