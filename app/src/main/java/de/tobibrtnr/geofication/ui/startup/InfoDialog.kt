package de.tobibrtnr.geofication.ui.startup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tobibrtnr.geofication.R

// Dialog that displays some information (here why a
// permission is needed) together with an info icon.
@Composable
fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
  AlertDialog(
    icon = {
      Icon(Icons.Filled.Info, contentDescription = "Info Icon")
    },
    title = {
      Text(text = title)
    },
    text = {
      Text(text = text)
    },
    onDismissRequest = {
      onDismiss()
    },
    confirmButton = {
      TextButton(
        onClick = {
          onDismiss()
        }
      ) {
        Text(stringResource(R.string.okay))
      }
    }
  )
}
