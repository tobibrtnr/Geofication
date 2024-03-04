package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil

fun processInput(
  enteredString: String,
  selectedGeofence: String,
  flags: List<String>,
  color: MarkerColor
) {

  var flagNum = 0
  if(flags.contains("entering")) {
    flagNum += 1
  }
  if(flags.contains("exiting")) {
    flagNum += 2
  }

  GeofenceUtil.addGeofication(
    enteredString,
    selectedGeofence,
    "",
    flagNum,
    0,
    true,
    color
  )
}

@Composable
fun AddGeoficationPopup(
  onDismissRequest: () -> Unit
) {

  var expanded by remember { mutableStateOf(false) }
  var selectedGeofence by remember { mutableStateOf("") }
  var selectedColor by remember { mutableStateOf(MarkerColor.RED) }
  var colorExpanded by remember { mutableStateOf(false) }
  var flags by remember {mutableStateOf(emptyList<String>())}
  var name by remember { mutableStateOf("") }
  var geofences by remember { mutableStateOf(emptyList<Geofence>()) }

  LaunchedEffect(Unit) {
    geofences = GeofenceUtil.getGeofences()
    println(geofences)
  }

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(400.dp)
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("Add a new Geofication")

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
              1.dp, Color(0xFF000000)
            ),
          placeholder = { Text("Enter Geofication name") },
          singleLine = true,
          shape = MaterialTheme.shapes.medium,
          value = name,
          onValueChange = {
            name = it//.take(max) for max name length
          }
        )

        SegmentedButtons("entering", "exiting") { flags = it }

        Text(
          text = selectedGeofence.ifEmpty { "Please select a geofence." },
          modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(16.dp)
        )

        Row(
          modifier = Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          // Geofence dropdown menu
          DropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = { expanded = false },
          ) {
            geofences.forEach {
              DropdownMenuItem(text = { Text(it.gid) }, onClick = {
                selectedGeofence = it.gid
                expanded = false
              })
            }
          }
        }

        CircleWithColor(
          color = selectedColor.color,
          radius = 10.dp,
          modifier = Modifier
            .clickable { colorExpanded = true }
            .padding(16.dp)
        )

        Row(
          modifier = Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          // Geofence dropdown menu
          DropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = colorExpanded,
            onDismissRequest = { colorExpanded = false },
          ) {
            MarkerColor.values().forEach {
              DropdownMenuItem(text = { CircleWithColor(color = it.color, radius = 10.dp) }, onClick = {
                selectedColor = it
                colorExpanded = false
              })
            }
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          TextButton(
            onClick = { onDismissRequest() },
            modifier = Modifier.padding(8.dp),
          ) {
            Text("Dismiss")
          }
          TextButton(
            onClick = {
              processInput(name, selectedGeofence, flags, selectedColor)
              onDismissRequest()
            },
            modifier = Modifier.padding(8.dp),
          ) {
            Text("Add")
          }
        }
      }
    }
  }
}
