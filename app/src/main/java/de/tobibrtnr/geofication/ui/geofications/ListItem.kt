package de.tobibrtnr.geofication.ui.geofications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.GeoficationScreen
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.ui.common.DeleteConfirmPopup
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import de.tobibrtnr.geofication.util.storage.LocaleUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Item of the Geofications list
 */
@Composable
fun ListItem(geofication: Geofication, navController: NavController, modifier: Modifier) {

  val context = LocalContext.current

  var deletePopupVisible by remember { mutableStateOf(false) }

  var geofence by remember { mutableStateOf<Geofence?>(null) }
  LaunchedEffect(geofication) {
    geofence = GeofenceUtil.getGeofenceById(geofication.fenceid)
  }

  if (geofence != null) {
    if (deletePopupVisible) {
      DeleteConfirmPopup(
        onConfirm = {
          deletePopupVisible = false
          CoroutineScope(Dispatchers.Default).launch {
            GeofenceUtil.deleteGeofence(geofence!!.id)
          }
        },
        onCancel = { deletePopupVisible = false }
      )
    }

    Card(
      modifier = modifier,
      colors = CardDefaults.cardColors(),
      onClick = {
        navController.navigate("${GeoficationScreen.Start.name}/${geofence!!.id}/false")
      }
    ) {
      Column(
        Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row {
            CircleWithColor(
              color = geofence!!.color.color,
              radius = 15.dp,
              modifier = Modifier.shadow(4.dp, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
              modifier = Modifier.fillMaxWidth(0.55f),
              text = geofication.message,
              style = MaterialTheme.typography.headlineSmall,
              fontStyle = if (geofication.active) FontStyle.Normal else FontStyle.Italic,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Switch(checked = geofication.active, onCheckedChange = {
              CoroutineScope(SupervisorJob()).launch {
                GeofenceUtil.setNotifActive(geofication.id, it)
              }
            })

            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = stringResource(R.string.edit),
              modifier = Modifier
                .size(32.dp)
                .clickable {
                  navController.navigate("${GeoficationScreen.Start.name}/${geofence!!.id}/true")
                }
            )

            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = stringResource(R.string.delete_geofication),
              modifier = Modifier
                .size(32.dp)
                .clickable {
                  if (geofication.active) {
                    deletePopupVisible = true
                  } else {
                    CoroutineScope(Dispatchers.Default).launch {
                      GeofenceUtil.deleteGeofence(geofence!!.id)
                    }
                  }
                }
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(geofence!!.fenceName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.trigger_count, geofication.triggerCount))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          stringResource(
            R.string.creation_time,
            LocaleUtil.getLocalDateTime(geofication.created, context)
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          stringResource(
            R.string.last_edit,
            LocaleUtil.getLocalDateTime(geofication.lastEdit, context)
          )
        )
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
  }
}