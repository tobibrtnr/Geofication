package de.tobibrtnr.geofication.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.LatLng
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.ui.common.SegmentedButtons
import de.tobibrtnr.geofication.ui.common.SegmentedRadioButtons
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.util.misc.NumericUnitTransformation
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import de.tobibrtnr.geofication.util.storage.UnitUtil
import kotlin.math.roundToInt

fun processInput(
  context: Context,
  name: String,
  radius: String,
  color: MarkerColor,
  pos: LatLng,
  message: String,
  flagList: List<String>,
  onTrigger: Int,
  delay: Int
) {

  val enteredFloat = try {
    radius.toFloat()
  } catch (e: NumberFormatException) {
    Toast.makeText(context, "Invalid radius input", Toast.LENGTH_SHORT).show()
    return
  }

  var flags = 0
  if (flagList.contains("entering")) {
    flags += 1
  }
  if (flagList.contains("exiting")) {
    flags += 2
  }

  println("GEOFENCE NAME: $name")

  val newGeofence = Geofence(
    fenceName = name,
    latitude = pos.latitude,
    longitude = pos.longitude,
    radius = (enteredFloat / UnitUtil.distanceFactor()).toFloat(),
    color = color,
    active = true,
    triggerCount = 0
  )

  val geofication = Geofication(
    fenceid = 0,
    message = message,
    flags = flags,
    delay = delay,
    repeat = true, // TODO, but not used right now
    active = true,
    onTrigger = onTrigger,
    triggerCount = 0
  )

  GeofenceUtil.addGeofence(
    context,
    newGeofence,
    geofication
  )
}

@Composable
fun AddGeofencePopup(
  pos: LatLng,
  rad: Double,
  onDismissRequest: () -> Unit,
) {

  val context = LocalContext.current

  var selectedColor by remember { mutableStateOf(MarkerColor.RED) }
  var colorExpanded by remember { mutableStateOf(false) }
  var radius by remember { mutableStateOf((rad * UnitUtil.distanceFactor()).toInt().toString()) }
  var name by remember { mutableStateOf("Unnamed Geofence") }

  var message by remember { mutableStateOf("") }

  var flags by remember { mutableStateOf(listOf("entering")) }
  var onTrigger by remember { mutableStateOf(1) }

  var delay by remember { mutableStateOf(0.0f) }

  var inputValid by remember { mutableStateOf(false) }

  val geocoder = Geocoder(context)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Implementation of GeocodeListener
    val listener = object : Geocoder.GeocodeListener {
      override fun onGeocode(addresses: MutableList<Address>) {
        if (addresses.size > 0) {
          name = "Geofence in ${getLocationName(addresses[0])}"
        }
      }

      override fun onError(errorMessage: String?) {
        println("GEOCODER ERROR $errorMessage")
      }
    }
    geocoder.getFromLocation(pos.latitude, pos.longitude, 1, listener)
  } else {
    val addresses = geocoder.getFromLocation(pos.latitude, pos.longitude, 1)
    name = if (addresses != null && addresses.size > 0) {
      "Geofence in ${getLocationName(addresses[0])}"
    } else {
      "Unnamed Geofence"
    }
  }

  Dialog(
    onDismissRequest = { onDismissRequest() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxHeight(0.85f)
        .fillMaxWidth(0.9f),
      //.height(450.dp)
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            "New Geofication",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Bold
          )

          Box(Modifier.clip(CircleShape)) {
            CircleWithColor(
              color = selectedColor.color,
              radius = 10.dp,
              modifier = Modifier
                .clickable { colorExpanded = true }
                .padding(16.dp)
            )
            DropdownMenu(
              modifier = Modifier.width(45.dp),
              expanded = colorExpanded,
              onDismissRequest = { colorExpanded = false },
            ) {
              MarkerColor.values().filter {
                it != selectedColor
              }.forEach {
                DropdownMenuItem(
                  text = { CircleWithColor(color = it.color, radius = 10.dp) },
                  onClick = {
                    selectedColor = it
                    colorExpanded = false
                  })
              }
            }
          }
        }

        Text(
          text = "Geofication title",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(CircleShape)
            .border(
              1.dp, Color(0xFF000000), CircleShape
            ),
          placeholder = { Text("Notification message") },
          singleLine = true,
          value = message,
          onValueChange = {
            message = it//.take(max) for max name length
            inputValid =
              message.isNotEmpty() && (radius.toFloatOrNull() != null) && (radius.toFloat() > 0)
          }
        )

        Text(
          text = "Geofence radius",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(CircleShape)
            .border(
              1.dp, Color(0xFF000000), CircleShape
            ),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
          ),
          shape = MaterialTheme.shapes.medium,
          value = radius,
          placeholder = { Text("Enter geofence radius") },
          singleLine = true,
          onValueChange = {
            val newValue = it.filter { char ->
              char.isDigit() || char == '.' //|| char == ','
            }
            radius = newValue
            inputValid =
              message.isNotEmpty() && (radius.toFloatOrNull() != null) && (radius.toFloat() > 0)
          },
          visualTransformation = NumericUnitTransformation()
        )

        Text(
          text = "Trigger event",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 16.dp)
        )

        SegmentedButtons("entering", "exiting") { flags = it }

        Text(
          text = "Behavior after triggering",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 16.dp)
        )

        SegmentedRadioButtons(
          opt1 = "stay active",
          opt2 = "disable",
          opt3 = "delete",
          onValueChange = {
            onTrigger = it
          }
        )

        Text(
          text = "Delay after Triggering",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 16.dp)
        )

        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "${delay.roundToInt()} minutes",
            modifier = Modifier.padding(start = 16.dp)
          )

          Spacer(modifier = Modifier.width(16.dp))

          Slider(
            modifier = Modifier.padding(end = 16.dp),
            value = delay,
            onValueChange = { delay = it },
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary,
              inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            valueRange = 0f..60f,
            steps = 11
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(
            onClick = { onDismissRequest() },
            modifier = Modifier.padding(8.dp),
          ) {
            Text("Cancel")
          }
          TextButton(
            onClick = {
              processInput(
                context,
                name,
                radius,
                selectedColor,
                pos,
                message,
                flags,
                onTrigger,
                delay.roundToInt()
              )
              onDismissRequest()
            },
            enabled = inputValid,
            modifier = Modifier.padding(8.dp),
          ) {
            Text("Save")
          }
        }
      }
    }
  }
}

fun getLocationName(address: Address): String {
  if (address.locality != null) {
    return address.locality
  }
  if (address.subAdminArea != null) {
    return address.subAdminArea
  }
  if (address.adminArea != null) {
    return address.adminArea
  }
  if (address.countryName != null) {
    return address.countryName
  }
  if (address.featureName != null) {
    return address.featureName
  }

  return "unnamed Area"
}
