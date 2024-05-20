package de.tobibrtnr.geofication.ui.infos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(navController: NavHostController) {

  val faqList = listOf(
    "How does the App work?" to "Geofication uses the geofence feature of Google Play Services. This allows the creation and handling of geofences in a very energy efficient and precise way.",
    "The notifications appear delayed or not at all." to "If this is the case, please check your system settings. Google Play Services have to be enabled and location enhancement needs to be activated. Also, having a kind of energy saving mode activated will impact the performance of the app, leading to geofences not being triggered in the worst case.",
    "Are there any limitations to the geofences?" to "Because of restrictions from the Google Play Services, a maximum of 100 geofences is allowed."
  )
  Scaffold(
    topBar = {
      TopAppBar(title = { Text("About, Info, FAQ") },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) {

    Box(modifier = Modifier
      .padding(start = 16.dp, end = 16.dp)
      .padding(it)) {
      Column {
        Text(
          text = "Here you can find further information about the app.",
          modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
        ) {
          items(faqList.size) { index ->
            FaqItem(question = faqList[index].first, answer = faqList[index].second)
          }
        }
      }
    }
  }
}

@Composable
fun FaqItem(question: String, answer: String) {
  var isExpanded by remember { mutableStateOf(false) }
  val rotationAngle by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    animationSpec = tween(durationMillis = 300), label = ""
  )

  Card(
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clickable { isExpanded = !isExpanded }
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = question,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
            .rotate(rotationAngle)
        )
      }
      AnimatedVisibility(visible = isExpanded) {
        Text(
          text = answer,
          fontSize = 16.sp,
          modifier = Modifier.padding(top = 8.dp)
        )
      }
    }
  }
}
