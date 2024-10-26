package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.GeoficationGeofence

@Composable
fun SearchResultList(
  query: MutableState<String>,
  searchGlobally: (String) -> Unit,
  goToLocation: (Double, Double, Float) -> Unit
) {
  val queryString by query

  var results by remember { mutableStateOf(emptyList<GeoficationGeofence>()) }

  LaunchedEffect(queryString) {
    results = if (queryString.isNotEmpty()) {
      GeofenceUtil.searchGeofications(queryString.trim())
    } else {
      emptyList()
    }
  }

  Column(
    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {

    if (queryString.isNotEmpty() && results.isEmpty()) {
      NoResultCard()
    } else if (results.isNotEmpty()) {
      SearchResultsCard(results = results, goToLocation)
    }
    SearchLocationCard(queryString, searchGlobally)
  }
}

@Composable
fun SearchResultsCard(results: List<GeoficationGeofence>, goToLocation: (Double, Double, Float) -> Unit) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(),
    modifier = Modifier
      .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp)),
  ) {
    results.forEachIndexed { index, result ->
      Column(
        Modifier
          .fillMaxWidth()
          .clickable {
            keyboardController?.hide()
            focusManager.clearFocus()
            goToLocation(result.latitude, result.longitude, result.radius)
          }) {
        Row(
          modifier = Modifier
            .padding(top = 12.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          CircleWithColor(
            modifier = Modifier.padding(end = 8.dp),
            color = result.color.color,
            radius = 8.dp
          )
          Text(
            text = result.message,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontStyle = if (result.active) FontStyle.Normal else FontStyle.Italic,
          )
        }
        Text(
          text = result.fenceName,
          modifier = Modifier
            .padding(bottom = 12.dp)
            .padding(horizontal = 16.dp),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.bodySmall,
          fontStyle = if (result.active) FontStyle.Normal else FontStyle.Italic,
        )
      }
      if (index != results.lastIndex) {
        HorizontalDivider(
          thickness = 1.dp,
          color = Color.Black,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
      }
    }
  }
}

@Composable
fun SearchLocationCard(query: String, searchGlobally: (String) -> Unit) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(),
    modifier = Modifier
      .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp)),
    onClick = {
      keyboardController?.hide()
      focusManager.clearFocus()
      searchGlobally(query)
    }
  ) {
    Box(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .wrapContentSize(Alignment.Center)
    ) {
      Text(stringResource(R.string.hit_enter_to_search))
    }
  }
}

@Composable
fun NoResultCard() {
  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(),
    modifier = Modifier
      .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp))
  ) {
    Box(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .wrapContentSize(Alignment.Center)
    ) {
      Text(stringResource(R.string.no_geofication_or_geofence_found))
    }
  }
}