package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import de.tobibrtnr.geofication.util.AppDatabase

enum class GeoficationScreen(val title: String) {
  Start(title = "Geofication"),
  Permissions(title = "Permissions"),
  Settings(title = "Settings"),
  Geofences(title = "Geofences")
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
            imageVector = Icons.Filled.ArrowBack,
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
          onClick = { showMenu = false; navController.navigate(GeoficationScreen.Settings.name) },
          text = { Text("Settings") })
      }
    }
  )
}

@Composable
fun GeoficationApp(
  navController: NavHostController = rememberNavController(),
  db: AppDatabase,
  geofencingClient: GeofencingClient,
  locationClient: FusedLocationProviderClient
) {
  // Get current back stack entry
  val backStackEntry by navController.currentBackStackEntryAsState()
  // Get the name of the curren screen
  val currentScreen =
    GeoficationScreen.valueOf(
      backStackEntry?.destination?.route ?: GeoficationScreen.Start.name
    )

  Scaffold(
    topBar = {
      GeoficationAppBar(
        currentScreen = currentScreen,
        canNavigateBack = navController.previousBackStackEntry != null,
        navController = navController
      )
    }
  ) { innerPadding ->

    NavHost(
      navController = navController,
      startDestination = GeoficationScreen.Start.name,
      modifier = Modifier
        .fillMaxSize()
        //.verticalScroll(rememberScrollState())
        .padding(innerPadding)
    ) {
      composable(route = GeoficationScreen.Start.name) {
        MapScreen(
          db = db,
          geofencingClient = geofencingClient,
          locationClient = locationClient,
          modifier = Modifier.fillMaxHeight()
        )
      }
      composable(route = GeoficationScreen.Settings.name) {
        SettingsScreen(
          modifier = Modifier.fillMaxHeight()
        )
      }
      composable(route = GeoficationScreen.Geofences.name) {
        GeofencesScreen(
          modifier = Modifier.fillMaxHeight()
        )
      }
    }
  }
}
