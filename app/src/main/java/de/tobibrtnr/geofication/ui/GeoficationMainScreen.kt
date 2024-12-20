package de.tobibrtnr.geofication.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.StringRes
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.BatterySavingPopup
import de.tobibrtnr.geofication.ui.geofications.GeoficationsScreen
import de.tobibrtnr.geofication.ui.map.MapScreen
import de.tobibrtnr.geofication.ui.settings.SettingsScreen
import de.tobibrtnr.geofication.ui.startup.DummyPermissionState
import de.tobibrtnr.geofication.ui.startup.PermissionScreen
import de.tobibrtnr.geofication.util.storage.setting.SettingsUtil

// Enum that holds all screens with their titles and icons for navigation.
enum class GeoficationScreen(@StringRes val title: Int, val icon: ImageVector) {
  Start(title = R.string.map, icon = Icons.Outlined.Map),
  Geofications(title = R.string.geofications, icon = Icons.Outlined.Notifications),
  Settings(title = R.string.settings, icon = Icons.Outlined.Settings)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeoficationApp(
  openGeoId: Int,
  intentQuery: String,
  navController: NavHostController = rememberNavController()
) {
  val context = LocalContext.current

  val geofenceViewModel = ServiceProvider.geofenceViewModel()
  val geoficationViewModel = ServiceProvider.geoficationViewModel()

  var navigationSelectedItem by remember {
    mutableIntStateOf(0)
  }

  // Permission to access coarse and fine location.
  val locPerm = rememberMultiplePermissionsState(
    permissions = listOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION
    ),
  )

  // Permission to send notifications.
  // Only needed starting with Android Tiramisu
  val notifPerm = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
    DummyPermissionState(
      "android.permission.POST_NOTIFICATIONS"
    )
  } else {
    rememberPermissionState(
      Manifest.permission.POST_NOTIFICATIONS
    )
  }

  // Permission to access background location.
  // Only needed starting with Android Q
  val bgPerm = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
    DummyPermissionState(
      "android.permission.ACCESS_BACKGROUND_LOCATION"
    )
  } else {
    rememberPermissionState(
      Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
  }

  // If all permissions have been granted
  var permissionsGranted by remember {
    mutableStateOf(
      locPerm.allPermissionsGranted && bgPerm.status.isGranted && notifPerm.status.isGranted
    )
  }

  val esEnabled = isPowerSaveMode(context)
  var isInPowerSaveMode by remember { mutableStateOf(esEnabled) }

  var navigateByBottomBar by remember { mutableStateOf(false) }

  // If not all permissions have been granted yet,
  // show PermissionScreen
  if (!permissionsGranted) {
    PermissionScreen(
      locPerm,
      bgPerm,
      notifPerm
    ) {
      permissionsGranted = true
    }
  } else {
    // Popup to notify user that battery saver mode is enabled
    if(isInPowerSaveMode && SettingsUtil.getPowerPopup()) {
      BatterySavingPopup(onConfirm = {
        openBatterySaverSettings(context)
        isInPowerSaveMode = false
      }, onCancel = {
        isInPowerSaveMode = false
      })
    }

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        NavigationBar {
          GeoficationScreen.entries.forEachIndexed { index, navItem ->
            val sel = index == navigationSelectedItem
            NavigationBarItem(
              selected = sel,
              label = {
                Text(
                  text = stringResource(navItem.title),
                  fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                )
              },
              onClick = {
                // On click on the navigation icon, navigate to the
                // desired screen and add the optional parameters.
                navigationSelectedItem = index
                var target = navItem.name
                if (target == GeoficationScreen.Start.name) {
                  navigateByBottomBar = true
                  target += "/0/false"
                }
                navController.navigate(target) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = { Icon(navItem.icon, contentDescription = stringResource(navItem.title)) })
          }
        }
      }
    ) { innerPadding ->
      val ld = LocalLayoutDirection.current
      NavHost(
        navController = navController,
        startDestination = "${GeoficationScreen.Start.name}/{openGeofence}/{edit}",
        modifier = Modifier.fillMaxSize()
      ) {
        // Map screen
        // Here, additional logic is needed in order to handle
        // the different, optional parameters.
        composable(
          route = "${GeoficationScreen.Start.name}/{openGeofence}/{edit}",
          arguments = listOf(
            navArgument("openGeofence") { defaultValue = 0 },
            navArgument("edit") { defaultValue = false }
          )
        ) { backStackEntry ->
          var openGeofence: Int? = null
          var edit: Boolean? = null
          var intentQueryString = intentQuery
          if (navigateByBottomBar) {
            navigateByBottomBar = false
            // No not move to intent query location if navigate
            intentQueryString = ""
          } else {
            if (navController.currentDestination?.route?.startsWith(GeoficationScreen.Start.name) == true) {
              // Manually set bottom navigation selected tab to "Map", if this screen
              // is really selected
              navigationSelectedItem = 0
            }
            // Get optional arguments if Geofication was selected
            openGeofence = backStackEntry.arguments?.getInt("openGeofence", 0)
            edit = backStackEntry.arguments?.getBoolean("edit", false)
            if (openGeofence == 0 && openGeoId != -1) {
              openGeofence = openGeoId
            }
          }
          Box(
            modifier = Modifier.padding(
              innerPadding.calculateStartPadding(ld),
              0.dp,
              innerPadding.calculateEndPadding(ld),
              innerPadding.calculateBottomPadding()
            )
          ) {
            MapScreen(
              topPadding = innerPadding.calculateTopPadding(),
              intentQuery = intentQueryString,
              openGeoId = openGeofence,
              edit = edit,
              geoficationViewModel = geoficationViewModel,
              geofenceViewModel = geofenceViewModel
            )
          }
        }
        // Settings screen
        composable(route = GeoficationScreen.Settings.name) {
          SettingsScreen(
            innerPadding = innerPadding,
            geofenceViewModel
          )
        }
        // Geofications list screen
        composable(route = GeoficationScreen.Geofications.name) {
          GeoficationsScreen(
            navController = navController,
            innerPadding = innerPadding,
            geoficationViewModel,
            geofenceViewModel
          )
        }
      }
    }
  }
}

// Check if the device is in power saver mode
@Composable
fun isPowerSaveMode(context: Context): Boolean {
  val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
  return powerManager.isPowerSaveMode
}

// Open settings to disable battery saver mode
fun openBatterySaverSettings(context: Context) {
  val intent =
    Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
  context.startActivity(intent)
}
