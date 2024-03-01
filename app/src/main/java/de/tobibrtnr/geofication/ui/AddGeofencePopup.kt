package de.tobibrtnr.geofication.ui

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.LatLng
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import kotlinx.coroutines.Job

fun processInput(
  context: Context,
  name: String,
  radius: String,
  color: MarkerColor,
  pos: LatLng,
  function: () -> Unit
) {

  val enteredFloat = try {
    radius.toFloat()
  } catch (e: NumberFormatException) {
    Toast.makeText(context, "Invalid radius input", Toast.LENGTH_SHORT).show()
    return
  }

  GeofenceUtil.addGeofence(
    context,
    name,
    pos.latitude,
    pos.longitude,
    enteredFloat,
    color
  )

  function()
}

@Composable
fun AddGeofencePopup(
  pos: LatLng,
  onDismissRequest: () -> Unit,
  function: () -> Unit
) {
  val context = LocalContext.current

  var selectedColor by remember { mutableStateOf(MarkerColor.RED) }
  var colorExpanded by remember { mutableStateOf(false) }
  var radius by remember {mutableStateOf("")}
  var name by remember { mutableStateOf("") }


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
        Text("Add a new Geofence")

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
              1.dp, Color(0xFF000000)
            ),
          shape = MaterialTheme.shapes.medium,
          value = name,
          onValueChange = {
            name = it//.take(max) for max name length
          }
        )

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
              1.dp, Color(0xFF000000)
            ),
          shape = MaterialTheme.shapes.medium,
          value = radius,
          onValueChange = {
            radius = it//.take(max) for max name length
          }
        )

        Text(
          text = selectedColor.name.lowercase(),
          modifier = Modifier
            .fillMaxWidth()
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
              DropdownMenuItem(text = { Text(it.name.lowercase()) }, onClick = {
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
              processInput(context, name, radius, selectedColor, pos, function)
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