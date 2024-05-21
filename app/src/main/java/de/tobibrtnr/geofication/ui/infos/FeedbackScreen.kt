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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavHostController) {
  Scaffold(
    topBar = {
      TopAppBar(title = { Text("Feedback") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
          text = "If you want to send feedback, you have multiple options:\n- You can open an issue on the official GitHub\n- You can give feedback on the Instagram account\n- You can rate the app on the Google Play Store\n- You can send feedback directly over the app",
          modifier = Modifier.padding(bottom = 16.dp)
        )
      }
    }
  }
}

