package de.tobibrtnr.geofication.ui

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.LatLng
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.Geofication

fun processInput(
  context: Context,
  name: String,
  radius: String,
  color: MarkerColor,
  pos: LatLng,
  message: String,
  flagList: List<String>
) {

  val enteredFloat = try {
    radius.toFloat()
  } catch (e: NumberFormatException) {
    Toast.makeText(context, "Invalid radius input", Toast.LENGTH_SHORT).show()
    return
  }

  var flags = 0
  if(flagList.contains("entering")) {
    flags += 1
  }
  if(flagList.contains("exiting")) {
    flags += 2
  }

  val newGeofence = Geofence(
    fenceName = name,
    latitude = pos.latitude,
    longitude = pos.longitude,
    radius = enteredFloat,
    color = color,
    active = true,
    triggerCount = 0
  )

  val geofication = Geofication(
    fenceid = 0,
    message = message,
    flags = flags,
    delay = 0, // TODO
    repeat = true, // TODO
    active = true,
    onTrigger = 1, // TODO
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
  var radius by remember {mutableStateOf("")}
  var name by remember { mutableStateOf("") }

  var message by remember { mutableStateOf("") }

  var flags by remember {mutableStateOf(listOf("entering"))}

  var inputValid by remember {mutableStateOf(false)}//(message/*, radius*/) { mutableStateOf(message.isNotEmpty())}// && (radius.toFloatOrNull() != null)) }

  val geocoder = Geocoder(context)

  if(rad != 0.0) {
    radius = rad.toInt().toString()
  }

  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Implementation of GeocodeListener
    val listener = object: Geocoder.GeocodeListener {
      override fun onGeocode(addresses: MutableList<Address>) {
        println(addresses)
        name = if (addresses.size > 0) {
          "Geofence in ${getLocationName(addresses[0])}"
        } else {
          "Unnamed Geofence"
        }
      }
      override fun onError(errorMessage: String?) {
        println(errorMessage)
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

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(450.dp)
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
          placeholder = { Text("Add notification message") },
          singleLine = true,
          shape = MaterialTheme.shapes.medium,
          value = message,
          onValueChange = {
            message = it//.take(max) for max name length
            inputValid = message.isNotEmpty() && (radius.toFloatOrNull() != null) && (radius.toFloat() > 0)
          }
        )

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
              1.dp, Color(0xFF000000)
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
            inputValid = message.isNotEmpty() && (radius.toFloatOrNull() != null) && (radius.toFloat() > 0)
          },
          visualTransformation = NumericUnitTransformation()
        )

        CircleWithColor(
          color = selectedColor.color,
          radius = 10.dp,
          modifier = Modifier
            .clickable { colorExpanded = true }
            .padding(16.dp)
        )

        SegmentedButtons("entering", "exiting") { flags = it }

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
              processInput(context, name, radius, selectedColor, pos, message, flags)
              onDismissRequest()
            },
            enabled = inputValid,
            modifier = Modifier.padding(8.dp),
          ) {
            Text("Add")
          }
        }
      }
    }
  }
}

@Composable
fun CircleWithColor(modifier: Modifier = Modifier, color: Color, radius: Dp) {
  Box(
    modifier = modifier
      .size(radius * 2)
      .clip(CircleShape)
      .background(color)
  )
}

@Composable
fun NumericUnitTransformation() = VisualTransformation { text ->
  TransformedText(
    text = buildAnnotatedString {
      append(text)
      append(" m") // Appending the unit 'm' after the number input
    },
    offsetMapping = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int {
        return offset
      }

      override fun transformedToOriginal(offset: Int): Int {
        return if (offset <= text.length) offset else text.length
      }
    }
  )
}

fun getLocationName(address: Address): String {
  if(address.locality != null) {
    return address.locality
  }
  if(address.subAdminArea != null) {
    return address.subAdminArea
  }
  if(address.adminArea != null) {
    return address.adminArea
  }
  if(address.countryName != null) {
    return address.countryName
  }
  if(address.featureName != null) {
    return address.featureName
  }

  return "unnamed Area"
}
