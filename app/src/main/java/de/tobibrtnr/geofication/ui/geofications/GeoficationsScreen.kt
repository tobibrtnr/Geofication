package de.tobibrtnr.geofication.ui.geofications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.DeleteAllConfirmPopup
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoficationsScreen(
  modifier: Modifier = Modifier,
  navController: NavController,
  geoViewModel: GeoficationsViewModel = viewModel()
) {

  val geoficationsArray by geoViewModel.geoficationsArray.collectAsState()

  var deleteAllPopupVisible by remember { mutableStateOf(false) }

  if (deleteAllPopupVisible) {
    DeleteAllConfirmPopup(
      onConfirm = {
        deleteAllPopupVisible = false
        CoroutineScope(Dispatchers.Default).launch {
          GeofenceUtil.deleteAllGeofications()
        }
      },
      onCancel = { deleteAllPopupVisible = false }
    )
  }

  // UI
  Scaffold(
    topBar = {
      TopAppBar(title = { Text(stringResource(R.string.geofications)) },
        actions = {
          if(geoficationsArray.isNotEmpty()) {
            IconButton(onClick = { deleteAllPopupVisible = true }) {
              Icon(
                imageVector = Icons.Filled.DeleteSweep,
                contentDescription = stringResource(R.string.delete_all_geofications_cd)
              )
            }
          }
        }
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .padding(top = paddingValues.calculateTopPadding(), start = 16.dp, end = 16.dp)
    ) {
      Column {
        Text(
          stringResource(
            R.string.geofication_created,
            geoficationsArray.size,
            if (geoficationsArray.size == 1) "" else "s"
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(visible = geoficationsArray.isEmpty()) {
          Text(stringResource(R.string.create_just_on_map))
        }

        AnimatedVisibility(visible = geoficationsArray.isNotEmpty()) {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(geoficationsArray, key = { it.id }) {
              ListItem(it, navController = navController, modifier = Modifier.animateItem())
            }
          }
        }
      }
    }
  }
}
