package de.tobibrtnr.geofication.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.LatLng
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.ui.common.SegmentedButtons
import de.tobibrtnr.geofication.ui.common.SegmentedRadioButtons
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.util.misc.NumericUnitTransformation
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import de.tobibrtnr.geofication.util.storage.UnitUtil
import java.util.regex.Pattern
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
  delay: Int,
  link: String
) {

  val enteredFloat = try {
    radius.toFloat()
  } catch (e: NumberFormatException) {
    Toast.makeText(context, context.getString(R.string.invalid_radius_input), Toast.LENGTH_SHORT)
      .show()
    return
  }

  var flags = 0
  if (flagList.contains("entering")) {
    flags += 1
  }
  if (flagList.contains("exiting")) {
    flags += 2
  }

  val newGeofence = Geofence(
    fenceName = name,
    latitude = pos.latitude,
    longitude = pos.longitude,
    radius = (enteredFloat / UnitUtil.distanceFactor()).toFloat(),
    color = color,
    active = true,
    triggerCount = 0,
    created = System.currentTimeMillis(),
    lastEdit = System.currentTimeMillis()
  )

  val geofication = Geofication(
    fenceid = 0,
    message = message,
    flags = flags,
    delay = delay,
    repeat = true, // TODO not used right now
    active = true,
    onTrigger = onTrigger,
    triggerCount = 0,
    created = System.currentTimeMillis(),
    lastEdit = System.currentTimeMillis(),
    link = link
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
  val initialName = stringResource(R.string.unnamed_geofence)

  val context = LocalContext.current

  var selectedColor by remember { mutableStateOf(MarkerColor.RED) }
  var colorExpanded by remember { mutableStateOf(false) }
  var radius by remember { mutableStateOf((rad * UnitUtil.distanceFactor()).toInt().toString()) }
  var name by remember { mutableStateOf(initialName) }

  var message by remember { mutableStateOf("") }

  var flags by remember { mutableStateOf(listOf("entering")) }
  var onTrigger by remember { mutableStateOf(1) }

  var delay by remember { mutableStateOf(0.0f) }

  var link by remember { mutableStateOf("") }

  var inputValid by remember { mutableStateOf(false) }

  val geocoder = Geocoder(context)

  val urlRegex = ".*" //"(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)"
  val urlPattern = Pattern.compile(urlRegex)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Implementation of GeocodeListener
    val listener = object : Geocoder.GeocodeListener {
      override fun onGeocode(addresses: MutableList<Address>) {
        if (addresses.size > 0) {
          name =
            context.getString(R.string.geofence_in_location, getLocationName(context, addresses[0]))
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
      context.getString(R.string.geofence_in_location, getLocationName(context, addresses[0]))
    } else {
      initialName
    }
  }

  Dialog(
    onDismissRequest = { onDismissRequest() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
      //  .fillMaxHeight(0.85f)
        .fillMaxWidth(0.9f),
      //.height(450.dp)
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            stringResource(R.string.new_geofication),
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
          text = stringResource(R.string.geofication_title),
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
            .clip(CircleShape)
            .border(
              1.dp, Color(0xFF000000), CircleShape
            ),
          placeholder = { Text(stringResource(R.string.notification_message)) },
          singleLine = true,
          value = message,
          onValueChange = {
            message = it//.take(max) for max name length
            val matcher = urlPattern.matcher(link)
            inputValid =
              message.isNotEmpty() &&
                  (radius.toFloatOrNull() != null) &&
                  (radius.toFloat() > 0) &&
                  (link.isEmpty() || matcher.matches())
          }
        )

        Text(
          text = stringResource(R.string.geofence_radius),
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 16.dp)
        )

        TextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .clip(CircleShape)
            .border(
              1.dp, Color(0xFF000000), CircleShape
            ),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
          ),
          shape = MaterialTheme.shapes.medium,
          value = radius,
          placeholder = { Text(stringResource(R.string.enter_geofence_radius)) },
          singleLine = true,
          onValueChange = {
            val newValue = it.filter { char ->
              char.isDigit() || char == '.' //|| char == ','
            }
            radius = newValue
            val matcher = urlPattern.matcher(link)
            inputValid =
              message.isNotEmpty() &&
                  (radius.toFloatOrNull() != null) &&
                  (radius.toFloat() > 0) &&
                  (link.isEmpty() || matcher.matches())
          },
          visualTransformation = NumericUnitTransformation()
        )

        CategoryItem(title = "Trigger Settings") {
          Column {
            Text(
              text = stringResource(R.string.trigger_event),
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(start = 16.dp)
            )

            SegmentedButtons(
              stringResource(R.string.entering),
              stringResource(R.string.exiting),
              "entering",
              "exiting",
              { flags = it.toList() },
              getFlagsFromList(flags)
            )

            Text(
              text = stringResource(R.string.behavior_after_triggering),
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(start = 16.dp)
            )

            SegmentedRadioButtons(
              opt1 = stringResource(R.string.stay_active),
              opt2 = stringResource(R.string.disable),
              opt3 = stringResource(R.string.delete),
              onValueChange = {
                onTrigger = it
              }
            )

            Text(
              text = stringResource(R.string.delay_after_triggering),
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(start = 16.dp)
            )

            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = stringResource(R.string.x_minutes, delay.roundToInt()),
                modifier = Modifier
                  .padding(start = 16.dp)
                  .width(90.dp)
              )

              Spacer(modifier = Modifier.width(16.dp))

              Slider(
                modifier = Modifier.padding(end = 16.dp),
                value = delay,
                onValueChange = {
                  if (delay != it) {
                    Vibrate.vibrate(context, 15)
                    delay = it
                  }
                },
                colors = SliderDefaults.colors(
                  thumbColor = MaterialTheme.colorScheme.primary,
                  activeTrackColor = MaterialTheme.colorScheme.primary,
                  inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                valueRange = 0f..60f,
                steps = 11
              )
            }
          }
        }

        CategoryItem("Extras") {
          Column {
            Text(
              text = "Link (TODO INFO ICON HERE)",
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(start = 16.dp)
            )

            TextField(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
                .clip(CircleShape)
                .border(
                  1.dp, Color(0xFF000000), CircleShape
                ),
              placeholder = { Text("https://www.example.com") },
              singleLine = true,
              value = link,
              onValueChange = {
                link = it//.take(max) for max name length
                val matcher = urlPattern.matcher(link)
                inputValid =
                  message.isNotEmpty() &&
                      (radius.toFloatOrNull() != null) &&
                      (radius.toFloat() > 0) &&
                      (link.isEmpty() || matcher.matches())
              }
            )
          }
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
            Text(stringResource(R.string.cancel))
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
                delay.roundToInt(),
                link
              )
              onDismissRequest()
            },
            enabled = inputValid,
            modifier = Modifier.padding(8.dp),
          ) {
            Text(stringResource(R.string.save))
          }
        }
      }
    }
  }
}

@Composable
fun CategoryItem(title: String, content: @Composable () -> Unit) {
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
          text = title,
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
        Column(modifier = Modifier.animateContentSize()) {
          key(isExpanded) {
            content()
          }
        }
      }
    }
  }
}

fun getLocationName(context: Context, address: Address): String {
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

  return context.getString(R.string.unnamed_area)
}
