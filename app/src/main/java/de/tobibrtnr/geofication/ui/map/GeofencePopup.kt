package de.tobibrtnr.geofication.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.ui.common.DeleteConfirmPopup
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.ui.common.SegmentedButtons
import de.tobibrtnr.geofication.ui.common.SegmentedRadioButtons
import de.tobibrtnr.geofication.ui.startup.InfoDialog
import de.tobibrtnr.geofication.util.misc.NumericUnitTransformation
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.UnitUtil
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import java.util.regex.Pattern
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun GeofencePopup(
  pGeofence: Geofence,
  pGeofication: Geofication,
  geofenceViewModel: GeofenceViewModel,
  editMode: Boolean,
  onSaveRequest: (Context, GeofenceViewModel, Geofence, Geofication) -> Unit,
  onDismissRequest: () -> Unit,
  onDeleteRequest: () -> Unit = {}
) {
  val initialName = stringResource(R.string.unnamed_geofence)
  val popupTitle = if (editMode) {
    stringResource(R.string.edit)
  } else {
    stringResource(R.string.new_geofication)
  }

  val context = LocalContext.current

  var geofication by remember { mutableStateOf(pGeofication) }
  var geofence by remember { mutableStateOf(pGeofence) }

  var colorExpanded by remember { mutableStateOf(false) }
  var radiusText by remember { mutableStateOf((geofence.radius * UnitUtil.distanceFactor()).toInt().toString()) }

  var deletePopupVisible by remember { mutableStateOf(false) }

  val (initialErrorMessage, initialInputValid) = validateInput(
    geofication,
    radiusText,
    ""
  )

  var inputValid by remember { mutableStateOf(initialInputValid) }
  var errorMessage by remember { mutableStateOf(initialErrorMessage) }

  var infoDialogVisible by remember { mutableStateOf(false) }


  val scrollState = rememberScrollState()

  val geocoder = Geocoder(context)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Implementation of GeocodeListener
    val listener = object : Geocoder.GeocodeListener {
      override fun onGeocode(addresses: MutableList<Address>) {
        if (addresses.isNotEmpty()) {
          geofence = geofence.copy(fenceName =
            context.getString(R.string.geofence_in_location, getLocationName(context, addresses[0])))
        }
      }

      override fun onError(errorMessage: String?) {
        println("GEOCODER ERROR $errorMessage")
      }
    }
    geocoder.getFromLocation(geofence.latitude, geofence.longitude, 1, listener)
  } else {
    val addresses = geocoder.getFromLocation(geofence.latitude, geofence.longitude, 1)
    geofence = geofence.copy(fenceName = if (!addresses.isNullOrEmpty()) {
      context.getString(R.string.geofence_in_location, getLocationName(context, addresses[0]))
    } else {
      initialName
    })
  }


  if (deletePopupVisible) {
    DeleteConfirmPopup(
      onConfirm = {
        deletePopupVisible = false
        onDeleteRequest()
      },
      onCancel = { deletePopupVisible = false }
    )
  }

  if (infoDialogVisible) {
    InfoDialog(
      title = stringResource(R.string.link_to_open),
      text = stringResource(R.string.link_explanation)
    ) {
      infoDialogVisible = false
    }
  }

  Dialog(
    onDismissRequest = { onDismissRequest() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp)
          .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
        ) {
          Text(
            popupTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Bold
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {

            if (editMode) {
              // Active State
              Switch(checked = geofication.active, onCheckedChange = {
                geofication = geofication.copy(active = it)
                geofence = geofence.copy(active = it)
              })

              // Delete Geofication
              Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_geofication),
                modifier = Modifier
                  .size(32.dp)
                  .clickable {
                    if (geofence.active) {
                      deletePopupVisible = true
                    } else {
                      onDeleteRequest()
                    }
                  }
              )
            }

            // Marker Color
            Box(Modifier.clip(CircleShape)) {
              CircleWithColor(
                color = geofence.color.color,
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
                  it != geofence.color
                }.forEach {
                  DropdownMenuItem(
                    text = { CircleWithColor(color = it.color, radius = 10.dp) },
                    onClick = {
                      geofence = geofence.copy(color = it)
                      colorExpanded = false
                    })
                }
              }
            }
          }
        }

        // Error hint if a property is not set correctly
        AnimatedVisibility(visible = !inputValid) {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = CircleShape,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer.copy(alpha=0.12f))
          ) {
            Text(
              text = errorMessage,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              style = MaterialTheme.typography.labelLarge
            )
          }
          Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
          singleLine = true,
          shape = CircleShape,
          value = geofication.message,
          onValueChange = {
            geofication = geofication.copy(message = it)

            val (newEM, newIV) = validateInput(geofication, radiusText, errorMessage)
            errorMessage = newEM
            inputValid = newIV
          },
          label = { Text(stringResource(R.string.notification_message)) },
          trailingIcon = {
            if(geofication.message.isNotEmpty()) {
              IconButton(onClick = {
                geofication = geofication.copy(message = "")

                val (newEM, newIV) = validateInput(geofication, radiusText, errorMessage)
                errorMessage = newEM
                inputValid = newIV
              }) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = stringResource(R.string.clear_text)
                )
              }
            }
          }
        )

        if (editMode) {
          OutlinedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            singleLine = true,
            shape = CircleShape,
            value = geofence.fenceName,
            onValueChange = {
              geofence = geofence.copy(fenceName = it)

              val (newEM, newIV) = validateInput(geofication, radiusText, errorMessage)
              errorMessage = newEM
              inputValid = newIV
            },
            label = { Text(stringResource(R.string.geofence_name)) },
            trailingIcon = {
              if (geofication.message.isNotEmpty()) {
                IconButton(onClick = {
                  geofence = geofence.copy(fenceName = "")

                  val (newEM, newIV) = validateInput(geofication, radiusText, errorMessage)
                  errorMessage = newEM
                  inputValid = newIV
                }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.clear_text)
                  )
                }
              }
            }
          )
        }

        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
          ),
          value = radiusText,
          label = { Text(stringResource(R.string.geofence_radius)) },
          shape = CircleShape,
          singleLine = true,
          onValueChange = {
            val newValue = it.filter { char ->
              char.isDigit() || char == '.' || char == ','
            }
            radiusText = newValue

            val (newEM, newIV) = validateInput(geofication, radiusText, errorMessage)
            errorMessage = newEM
            inputValid = newIV
          },
          visualTransformation = NumericUnitTransformation()
        )

        CategoryItem(title = "Trigger Settings") {
          Column {
            Text(
              text = stringResource(R.string.trigger_event),
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier
            )

            SegmentedButtons(
              stringResource(R.string.entering),
              stringResource(R.string.exiting),
              "entering",
              "exiting",
              { geofication = geofication.copy(flags = getFlagsFromList(it.toList())) },
              geofication.flags
            )

            Spacer(Modifier.height(12.dp))

            Text(
              text = stringResource(R.string.behavior_after_triggering),
              style = MaterialTheme.typography.bodyLarge
            )

            SegmentedRadioButtons(
              opt1 = stringResource(R.string.stay_active),
              opt2 = stringResource(R.string.disable_capitalized),
              opt3 = stringResource(R.string.delete),
              onValueChange = {
                geofication = geofication.copy(onTrigger = it)
              }
            )

            Spacer(Modifier.height(12.dp))

            Text(
              text = stringResource(R.string.delay_after_triggering),
              style = MaterialTheme.typography.bodyLarge
            )

            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = stringResource(R.string.x_minutes, geofication.delay),
                modifier = Modifier
                  .padding(start = 16.dp)
                  .width(90.dp)
              )

              Spacer(modifier = Modifier.width(16.dp))

              Slider(
                value = geofication.delay.toFloat(),
                onValueChange = {
                  if (geofication.delay.toFloat() != it) {
                    Vibrate.vibrate(context, 15)
                    geofication = geofication.copy(delay = it.roundToInt())
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

        Spacer(Modifier.height(4.dp))

        CategoryItem("Extras") {
          Column {
            Row(
              modifier = Modifier.height(IntrinsicSize.Min)
            ) {
              OutlinedTextField(
                modifier = Modifier
                  .weight(1f),
                label = { Text(stringResource(R.string.link_to_open)) },
                placeholder = { Text("https://www.example.com") },
                singleLine = true,
                shape = CircleShape,
                value = geofication.link,
                onValueChange = {
                  geofication = geofication.copy(link = it)

                  val (newEM, newIV) = validateInput(geofication, radiusText, errorMessage)
                  errorMessage = newEM
                  inputValid = newIV
                },
                trailingIcon = {
                  if(geofication.link.isNotEmpty()) {
                    IconButton(onClick = {
                      geofication = geofication.copy(link = "")

                      val (newEM, newIV) = validateInput(geofication, radiusText, errorMessage)
                      errorMessage = newEM
                      inputValid = newIV
                    }) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_text)
                      )
                    }
                  }
                }
              )
              Spacer(Modifier.width(4.dp))
              IconButton(modifier = Modifier.fillMaxHeight(), onClick = {
                infoDialogVisible = true
              }) {
                Icon(
                  imageVector = Icons.Outlined.Info,
                  contentDescription = stringResource(R.string.show_info)
                )
              }
            }
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
              geofence = geofence.copy(
                radius = (radiusText.toFloat() / UnitUtil.distanceFactor()).toFloat()
              )
              onSaveRequest(
                context,
                geofenceViewModel,
                geofence,
                geofication
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
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded },
  ) {
    Column(
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .weight(1f)
            .padding(vertical = 10.dp)
        )
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
            .rotate(rotationAngle)
        )
      }
      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.animateContentSize()) {
          content()
        }
      }
    }
  }
}

fun validateInput(
  geofication: Geofication,
  radiusText: String,
  errorMessage: String
): Pair<String, Boolean> {
  // Minimum geofence radius is 30 m, maximum is 1000 km
  val minValue = UnitUtil.appendUnit(30)
  val maxValue = UnitUtil.appendUnit(1000000)

  // Link format
  val urlRegex = "(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)"
  val matcher = Pattern.compile(urlRegex).matcher(geofication.link)

  if (geofication.message.isEmpty()) {
    return Pair("Please enter a notification message.", false)
  }
  if (radiusText.toFloatOrNull() == null) {
    return Pair("Please enter a valid radius.", false)
  }
  val fac = UnitUtil.distanceFactor()
  if (radiusText.toFloat() !in floor(30.0 * fac)..floor(1000000.0 * fac)) {
    return Pair("Please enter a radius between $minValue and $maxValue.", false)
  }
  if (geofication.link.isNotEmpty() && !matcher.matches()) {
    return Pair("Please enter a valid link format.", false)
  }

  // No errors, input is valid
  return Pair(errorMessage, true)
}

fun getFlagsFromList(list: List<String>): Int {
  var flags = 0
  if (list.contains("entering")) {
    flags += 1
  }
  if (list.contains("exiting")) {
    flags += 2
  }

  return flags
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