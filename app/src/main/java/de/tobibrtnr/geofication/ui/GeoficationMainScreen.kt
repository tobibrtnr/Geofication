package de.tobibrtnr.geofication.ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

enum class GeoficationScreen(val title: String, val icon: ImageVector) {
  Start(title = "Start", icon = Icons.Outlined.Map),
  Geofences(title = "Geofences", icon = Icons.Outlined.Circle),
  Geofications(title = "Geofications", icon = Icons.Outlined.Notifications),
  Settings(title = "Settings", icon = Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoficationAppBar(
  currentScreen: GeoficationScreen,
  canNavigateBack: Boolean,
  navController: NavHostController,
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }

  TopAppBar(
    title = { Text(currentScreen.title) },
    modifier = modifier,
    navigationIcon = {
      if (canNavigateBack) {
        // Back Button always navigates to start for now
        IconButton(onClick = { navController.navigate(GeoficationScreen.Start.name) }) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }
      }
    },
    actions = {
      IconButton(onClick = { showMenu = !showMenu }) {
        Icon(
          imageVector = Icons.Filled.MoreVert,
          contentDescription = "Menu"
        )
      }

      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
      ) {
        DropdownMenuItem(
          onClick = { showMenu = false; navController.navigate(GeoficationScreen.Geofences.name) },
          text = { Text("View Geofences") })
        DropdownMenuItem(
          onClick = {
            showMenu = false; navController.navigate(GeoficationScreen.Geofications.name)
          },
          text = { Text("View Geofications") })
        DropdownMenuItem(
          onClick = { showMenu = false; navController.navigate(GeoficationScreen.Settings.name) },
          text = { Text("Settings") })
      }
    }
  )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeoficationApp(
  navController: NavHostController = rememberNavController()
) {

  var navigationSelectedItem by remember {
    mutableStateOf(0)
  }

  val permissions = rememberMultiplePermissionsState(
    permissions = listOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.POST_NOTIFICATIONS
    ),
  )

  val backgroundPermission = rememberPermissionState(
    Manifest.permission.ACCESS_BACKGROUND_LOCATION
  )

  var permissionsGranted by remember {
    mutableStateOf(
      permissions.allPermissionsGranted && backgroundPermission.status.isGranted
    )
  }

  if (!permissionsGranted) {
    PermissionScreen(
      permissions,
      backgroundPermission
    ) {
      permissionsGranted = true
    }
  } else {

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        NavigationBar {
          GeoficationScreen.values().forEachIndexed { index, navItem ->
            val sel = index == navigationSelectedItem
            NavigationBarItem(
              selected = sel,
              label = {
                Text(
                  text = navItem.title,
                  fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                )
              },
              onClick = {
                navigationSelectedItem = index
                navController.navigate(navItem.name) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = { Icon(navItem.icon, contentDescription = navItem.title) })
          }
        }
      }
    ) { innerPadding ->
      val ld = LocalLayoutDirection.current
      NavHost(
        navController = navController,
        startDestination = GeoficationScreen.Start.name,
        modifier = Modifier
          .fillMaxSize()
        //.verticalScroll(rememberScrollState())
        //.padding(innerPadding)
      ) {
        composable(route = GeoficationScreen.Start.name) {
          Box(
            modifier = Modifier.padding(
              innerPadding.calculateStartPadding(ld),
              0.dp,
              innerPadding.calculateEndPadding(ld),
              innerPadding.calculateBottomPadding()
            )
          ) {
            MapScreen(
              modifier = Modifier.fillMaxHeight(),
              topPadding = innerPadding.calculateTopPadding()
            )
          }
        }
        composable(route = GeoficationScreen.Settings.name) {
          Box(modifier = Modifier.padding(innerPadding)) {
            SettingsScreen(
              modifier = Modifier.fillMaxSize()
            )
          }
        }
        composable(route = GeoficationScreen.Geofences.name) {
          Box(modifier = Modifier.padding(innerPadding)) {
            GeofencesScreen(
              modifier = Modifier.fillMaxSize()
            )
          }
        }
        composable(route = GeoficationScreen.Geofications.name) {
          Box(modifier = Modifier.padding(innerPadding)) {
            GeoficationsScreen(
              modifier = Modifier.fillMaxSize()
            )
          }
        }
      }
    }
  }
}
