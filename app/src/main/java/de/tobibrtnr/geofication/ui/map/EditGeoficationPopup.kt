package de.tobibrtnr.geofication.ui.map

import android.content.Context
import androidx.compose.runtime.Composable
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.geofication.Geofication

// Function to update an existing Geofication and geofence.
fun processEdit(
  context: Context,
  geofenceViewModel: GeofenceViewModel,
  geofence: Geofence,
  geofication: Geofication
) {
  // Update last edit time for both objects
  geofence.lastEdit = System.currentTimeMillis()
  geofication.lastEdit = System.currentTimeMillis()

  geofenceViewModel.addGeofence(
    context,
    geofence,
    geofication,
    true
  )
}

// A composable that display a popup where
// the user can edit their Geofication.
@Composable
fun EditGeoficationPopup(
  geofence: Geofence,
  geofication: Geofication,
  geofenceViewModel: GeofenceViewModel,
  onDismissRequest: () -> Unit,
  onDeleteRequest: () -> Unit
) {
  GeofencePopup(
    geofence,
    geofication,
    geofenceViewModel,
    true,
    ::processEdit,
    onDismissRequest,
    onDeleteRequest
  )
}
