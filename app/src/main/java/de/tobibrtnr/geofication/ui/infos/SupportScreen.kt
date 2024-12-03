package de.tobibrtnr.geofication.ui.infos

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.tobibrtnr.geofication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavHostController) {
  val context = LocalContext.current

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
          modifier = Modifier.padding(bottom = 8.dp),
          text = stringResource(R.string.give_support_and_feedback)
        )

        // Home Page
        SupportScreenButton(
          iconId = R.drawable.ic_notification,
          iconDesc = "Homepage Icon",
          buttonText = stringResource(R.string.visit_the_app_homepage),
          onClick = {
            openLink("https://geofication.tobibrtnr.de", context)
          }
        )

        // Github Repository
        SupportScreenButton(
          iconId = R.drawable.github,
          iconDesc = "GitHub Icon",
          buttonText = stringResource(R.string.visit_the_github_repository),
          onClick = {
            openLink("https://github.com/tobibrtnr/Geofication", context)
          }
        )

        // Google Play Store page
        SupportScreenButton(
          iconId = R.drawable.google_play,
          iconDesc = "Google Play Store icon",
          buttonText = stringResource(R.string.rate_the_app_in_the_google_play_store),
          onClick = {
            openLink("https://play.google.com/store/apps/details?id=de.tobibrtnr.geofication", context)
          }
        )

        // Contact via email
        SupportScreenButton(
          iconId = R.drawable.email,
          iconDesc = "Email icon",
          buttonText = stringResource(R.string.contact_via_email),
          onClick = {
            openEmail("mailto:hello@tobibrtnr.de", context)
          }
        )
      }
    }
  }
}

// A custom button with a given style suiting to the screen.
@Composable
fun SupportScreenButton(iconId: Int, iconDesc: String, buttonText: String, onClick: () -> Unit) {
  Button(
    modifier = Modifier.fillMaxWidth(),
    onClick = {
      onClick()
    },
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Icon(
      painter = painterResource(id = iconId),
      contentDescription = iconDesc,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(buttonText)
  }
}

// Open a given link in the browser.
fun openLink(link: String, context: Context) {
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
  context.startActivity(intent)
}

// Open a new email to the given address.
fun openEmail(email: String, context: Context) {
  val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse(email)
  }
  context.startActivity(intent)
}
