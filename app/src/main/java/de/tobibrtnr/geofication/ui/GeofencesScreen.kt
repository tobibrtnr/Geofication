package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.util.AppDatabase
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil

@Composable
fun GeofencesScreen(
  modifier: Modifier = Modifier,
  db: AppDatabase
) {

  var geofencesArray by remember { mutableStateOf(emptyList<Geofence>()) }
  LaunchedEffect(Unit) {
    val geofences = GeofenceUtil.getGeofences(db)
    geofencesArray = geofences
  }

  LazyColumn {
    items(geofencesArray) {
      ListItem(it)
    }
  }
}

@Composable
fun ListItem(geofence: Geofence) {
  Column(modifier = Modifier.padding(16.dp)) {
    Text(geofence.gid)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Radius: ${geofence.radius}m")
  }
}
