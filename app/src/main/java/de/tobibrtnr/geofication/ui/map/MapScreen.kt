package de.tobibrtnr.geofication.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.tobibrtnr.geofication.ui.infos.FaqScreen
import de.tobibrtnr.geofication.ui.infos.SupportScreen
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.geofication.GeoficationViewModel

@Composable
fun MapScreen(
  modifier: Modifier = Modifier,
  topPadding: Dp,
  openGeoId: Int?,
  edit: Boolean?,
  intentQuery: String,
  geofenceViewModel: GeofenceViewModel,
  geoficationViewModel: GeoficationViewModel
) {
  val mapNavController = rememberNavController()

  // Always go to map view when opening tab
  mapNavController.navigateUp()

  NavHost(navController = mapNavController, startDestination = "mapMain") {
    composable("mapMain") {
      MapScreenMain(
        topPadding = topPadding,
        openGeoId = openGeoId,
        edit = edit,
        intentQuery = intentQuery,
        navController = mapNavController,
        geofenceViewModel = geofenceViewModel,
        geoficationViewModel = geoficationViewModel
      )
    }
    composable("faq") {
      FaqScreen(
        navController = mapNavController
      )
    }
    composable("support") {
      SupportScreen(
        navController = mapNavController
      )
    }
  }
}
