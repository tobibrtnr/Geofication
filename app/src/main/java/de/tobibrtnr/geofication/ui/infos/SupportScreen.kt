package de.tobibrtnr.geofication.ui.infos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.tobibrtnr.geofication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavHostController) {
  Scaffold(
    topBar = {
      TopAppBar(title = { Text(stringResource(R.string.support)) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back)
            )
          }
        }
      )
    }
  ) {

    Box(
      modifier = Modifier
        .padding(start = 16.dp, end = 16.dp)
        .padding(it)
    ) {
      Column {
        Text(
          text = stringResource(R.string.support_developer),
          modifier = Modifier.padding(bottom = 16.dp)
        )
      }
    }
  }
}

