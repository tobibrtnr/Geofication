package de.tobibrtnr.geofication.ui.geofications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.GeoficationScreen
import de.tobibrtnr.geofication.ui.common.DeleteConfirmPopup
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.setting.LocaleUtil
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofication.Geofication

/**
 * Item of the Geofications list
 */
@Composable
fun ListItem(
  geofication: Geofication,
  geofence: Geofence,
  navController: NavController,
  modifier: Modifier,
  delete: (Int) -> Unit,
  setActive: (Int, Int, Boolean) -> Unit
) {

  val context = LocalContext.current

  var deletePopupVisible by remember { mutableStateOf(false) }

  if (deletePopupVisible) {
    DeleteConfirmPopup(
      onConfirm = {
        deletePopupVisible = false
        delete(geofence.id)
      },
      onCancel = { deletePopupVisible = false }
    )
  }

  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(),
    onClick = {
      navController.navigate("${GeoficationScreen.Start.name}/${geofence.id}/false")
    }
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .background(geofence.color.color.copy(alpha=0.075f))
        .padding(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = geofication.message,
          style = MaterialTheme.typography.headlineSmall,
          fontStyle = if (geofication.active) FontStyle.Normal else FontStyle.Italic,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Row(
          modifier = Modifier.wrapContentWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

          IconButton(onClick = {
            navController.navigate("${GeoficationScreen.Start.name}/${geofence.id}/true")
          }) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = stringResource(R.string.edit)
            )
          }

          IconButton(onClick = {
            if (geofication.active) {
              deletePopupVisible = true
            } else {
              delete(geofence.id)
            }
          }) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = stringResource(R.string.delete_geofication)
            )
          }

          Switch(checked = geofication.active, onCheckedChange = {
            Vibrate.vibrate(context, 50)
            setActive(geofence.id, geofication.id, it)
          })
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(geofence.fenceName, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(modifier = Modifier.height(4.dp))
      Text(stringResource(R.string.trigger_count, geofication.triggerCount))
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        stringResource(
          R.string.creation_time,
          LocaleUtil.getLocalDateTime(geofication.created, context)
        )
      )
    }
  }
  Spacer(modifier = Modifier.height(8.dp))
}