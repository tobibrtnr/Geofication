package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tobibrtnr.geofication.util.AppDatabase
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.Geofication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Composable
fun GeoficationsScreen(
  modifier: Modifier = Modifier,
  navController: NavController
) {

  var geoficationsArray by remember { mutableStateOf(emptyList<Geofication>()) }

  @Composable
  fun refreshGeofications() {
    LaunchedEffect(Unit) {
      val geofications = GeofenceUtil.getGeofications()
      geoficationsArray = geofications
    }
  }
  refreshGeofications()

  // UI
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Text(text = "Geofications", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text("${geoficationsArray.size} Geofication${if (geoficationsArray.size == 1) "" else "s"} created.")
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn {
      items(geoficationsArray) {
        ListItem(it, navController = navController) {
          val geofications = GeofenceUtil.getGeofications()
          geoficationsArray = geofications
        }
      }

    }
  }
}

@Composable
fun ListItem(geofication: Geofication, navController: NavController, refreshData: suspend () -> Unit) {

  var geofence by remember { mutableStateOf<Geofence?>(null) }
  LaunchedEffect(Unit) {
    geofence = GeofenceUtil.getGeofenceById(geofication.fenceid)
  }

  if (geofence != null) {
    Card(
      colors = CardDefaults.cardColors(),
      onClick = {
        navController.navigate("${GeoficationScreen.Start.name}/${geofence!!.id}")
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
              CircleWithColor(color = geofence!!.color.color, radius = 15.dp, modifier = Modifier.shadow(4.dp, CircleShape))
              Spacer(Modifier.width(8.dp))
              Text(
                modifier = Modifier.fillMaxWidth(0.65f),
                text = geofication.message,
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = if (geofication.active) FontStyle.Normal else FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
          }

          Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = geofication.active, onCheckedChange = {
              CoroutineScope(SupervisorJob()).launch {
                GeofenceUtil.setNotifActive(geofication.id, it)
                refreshData()
              }
            })

            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = "Delete Geofication",
              modifier = Modifier
                .size(32.dp)
                .clickable {
                  CoroutineScope(Dispatchers.Default).launch {
                    GeofenceUtil.deleteGeofence(geofence!!.id)
                    refreshData()
                  }
                }
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(geofence!!.fenceName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Trigger Count: ${geofication.triggerCount}")
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
  }
}