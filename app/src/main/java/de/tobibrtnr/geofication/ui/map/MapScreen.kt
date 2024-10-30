package de.tobibrtnr.geofication.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.tobibrtnr.geofication.ui.infos.FaqScreen
import de.tobibrtnr.geofication.ui.infos.SupportScreen

@Composable
fun MapScreen(
  modifier: Modifier = Modifier,
  topPadding: Dp,
  openGeoId: Int?,
  edit: Boolean?,
  intentQuery: String,
  mapViewModel: MapViewModel = viewModel(),
) {
  val mapNavController = rememberNavController()

  NavHost(navController = mapNavController, startDestination = "mapMain") {
    composable("mapMain") {
      MapScreenMain(
        topPadding = topPadding,
        openGeoId = openGeoId,
        edit = edit,
        intentQuery = intentQuery,
        mapViewModel = mapViewModel,
        navController = mapNavController
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
