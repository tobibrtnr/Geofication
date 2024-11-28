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
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            openLink("https://geofication.tobibrtnr.de", context)
          },
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.ic_notification),
            contentDescription = "Homepage Icon",
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.visit_the_app_homepage))
        }

        // Github Repository
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            openLink("https://github.com/tobibrtnr/Geofication", context)
          },
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.github),
            contentDescription = "GitHub Icon",
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.visit_the_github_repository))
        }

        // Google Play Store
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            openLink("https://play.google.com/store/apps/details?id=de.tobibrtnr.geofication", context)
          },
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.google_play),
            contentDescription = "Google Play Store icon",
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.rate_the_app_in_the_google_play_store))
        }

        // Watch Ad Video
        /*Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = { /* TODO */ },
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.gift),
            contentDescription = "Gift icon",
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Watch a paid ad")
        }*/

        // Email
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            openEmail("mailto:hello@tobibrtnr.de", context)
          },
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.email),
            contentDescription = "Email icon",
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.contact_via_email))
        }
      }
    }
  }
}

fun openLink(link: String, context: Context) {
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
  context.startActivity(intent)
}

fun openEmail(email: String, context: Context) {
  val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse(email)
  }
  context.startActivity(intent)
}
