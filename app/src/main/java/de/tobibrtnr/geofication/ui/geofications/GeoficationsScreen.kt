package de.tobibrtnr.geofication.ui.geofications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = stringResource(R.string.geofications),
        style = MaterialTheme.typography.headlineMedium
      )

      if(geoficationsArray.isNotEmpty()) {
        Icon(
          imageVector = Icons.Filled.DeleteSweep,
          contentDescription = stringResource(R.string.delete_all_geofications_cd),
          modifier = Modifier
            .size(32.dp)
            .clickable {
              deleteAllPopupVisible = true
            }
        )
      }

    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      stringResource(
        R.string.geofication_created,
        geoficationsArray.size,
        if (geoficationsArray.size == 1) "" else "s"
      )
    )
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn {
      items(geoficationsArray) {
        ListItem(it, navController = navController)
      }
    }
  }
}
