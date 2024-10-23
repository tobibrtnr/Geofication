package de.tobibrtnr.geofication.ui.map

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.ui.common.SegmentedButtons
import de.tobibrtnr.geofication.ui.common.SegmentedRadioButtons
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.ui.common.DeleteConfirmPopup
import de.tobibrtnr.geofication.util.misc.NumericUnitTransformation
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

fun processEdit(
  context: Context,
  geofence: Geofence,
  geofication: Geofication
) {

  geofence.active = geofication.active

  // Update last edit time for both objects
  geofence.lastEdit = System.currentTimeMillis()
  geofication.lastEdit = System.currentTimeMillis()

  GeofenceUtil.addGeofence(
    context,
    geofence,
    geofication,
    true
  )
}

@Composable
fun EditGeoficationPopup(
  selectedMarkerId: Int,
  onDismissRequest: () -> Unit,
  onDeleteRequest: () -> Unit
) {
  val context = LocalContext.current

  var initialGeofication by remember { mutableStateOf<Geofication?>(null) }
  var initialGeofence by remember { mutableStateOf<Geofence?>(null) }

  var selectedGeofication by remember { mutableStateOf<Geofication?>(null) }
  var selectedGeofence by remember { mutableStateOf<Geofence?>(null) }

  var enabled by remember { mutableStateOf(false) }
  var colorExpanded by remember { mutableStateOf(false) }
  var inputValid by remember { mutableStateOf(false) }

  var deletePopupVisible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    selectedGeofication = GeofenceUtil.getGeoficationByGeofence(selectedMarkerId)[0]
    selectedGeofence = GeofenceUtil.getGeofenceById(selectedMarkerId)

    initialGeofication = selectedGeofication!!.copy()
    initialGeofence = selectedGeofence!!.copy()
    enabled = selectedGeofication!!.active
  }

  // Check if input is valid: Geofence or Geofication attribute changed + all inputs valid
  fun isInputValid(): Boolean {
    if (selectedGeofication!! == initialGeofication && selectedGeofence!! == initialGeofence) {
      return false
    }

    return selectedGeofication!!.message.isNotEmpty() && (selectedGeofence!!.radius > 0)
  }

  // UI
  if (selectedGeofence != null && selectedGeofication != null) {
    if (deletePopupVisible) {
      DeleteConfirmPopup(
        onConfirm = {
          deletePopupVisible = false
          onDeleteRequest()
        },
        onCancel = { deletePopupVisible = false }
      )
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
              text = stringResource(R.string.edit),
              style = MaterialTheme.typography.headlineMedium,
              modifier = Modifier.padding(start = 16.dp),
              fontWeight = FontWeight.Bold
            )

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Switch(checked = selectedGeofication!!.active, onCheckedChange = {
                selectedGeofication = selectedGeofication!!.copy(active = it)
                inputValid = isInputValid()
              })

              Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_geofication),
                modifier = Modifier
                  .size(32.dp)
                  .clickable {
                    if (selectedGeofence!!.active) {
                      deletePopupVisible = true
                    } else {
                      onDeleteRequest()
                    }
                  }
              )

              Box(Modifier.clip(CircleShape)) {
                CircleWithColor(
                  color = selectedGeofence!!.color.color,
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
                    it != selectedGeofence!!.color
                  }.forEach {
                    DropdownMenuItem(
                      text = { CircleWithColor(color = it.color, radius = 10.dp) },
                      onClick = {
                        selectedGeofence = selectedGeofence!!.copy(color = it)
                        inputValid = isInputValid()
                        colorExpanded = false
                      })
                  }
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
              .padding(16.dp)
              .clip(CircleShape)
              .border(
                1.dp, Color(0xFF000000), CircleShape
              ),
            placeholder = { Text(stringResource(R.string.notification_message)) },
            singleLine = true,
            value = selectedGeofication!!.message,
            onValueChange = {
              selectedGeofication =
                selectedGeofication!!.copy(message = it)//.take(max) for max name length
              inputValid = isInputValid()
            },
            trailingIcon = {
              if(selectedGeofication!!.message.isNotEmpty()) {
                IconButton(onClick = {
                  selectedGeofication = selectedGeofication!!.copy(message = "")
                  inputValid = isInputValid()
                }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.clear_text)
                  )
                }
              }
            }
          )

          TextField(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
              .clip(CircleShape)
              .border(
                1.dp, Color(0xFF000000), CircleShape
              ),
            placeholder = { Text(stringResource(R.string.geofence_name)) },
            singleLine = true,
            value = selectedGeofence!!.fenceName,
            onValueChange = {
              selectedGeofence =
                selectedGeofence!!.copy(fenceName = it)//.take(max) for max name length
              inputValid = isInputValid()
            },
            trailingIcon = {
              if(selectedGeofence!!.fenceName.isNotEmpty()) {
                IconButton(onClick = {
                  selectedGeofence = selectedGeofence!!.copy(fenceName = "")
                  inputValid = isInputValid()
                }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.clear_text)
                  )
                }
              }
            }
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
            value = selectedGeofence!!.radius.toString(),
            placeholder = { Text(stringResource(R.string.enter_geofence_radius)) },
            singleLine = true,
            onValueChange = {
              val newValue = it.filter { char ->
                char.isDigit() || char == '.' //|| char == ','
              }.toFloatOrNull()
              if (newValue != null) {
                selectedGeofence = selectedGeofence!!.copy(radius = newValue)
              }
              inputValid = isInputValid()
            },
            visualTransformation = NumericUnitTransformation()
          )

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
            {
              selectedGeofication = selectedGeofication!!.copy(flags = getFlagsFromList(it))
              inputValid = isInputValid()
            },
            selectedGeofication!!.flags
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
              selectedGeofication = selectedGeofication!!.copy(onTrigger = it)
              inputValid = isInputValid()
            },
            onTrigger = selectedGeofication!!.onTrigger
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
              text = stringResource(R.string.x_minutes, selectedGeofication!!.delay),
              modifier = Modifier.padding(start = 16.dp).width(90.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Slider(
              modifier = Modifier.padding(end = 16.dp),
              value = selectedGeofication!!.delay.toFloat(),
              onValueChange = {
                if(selectedGeofication!!.delay != it.roundToInt()) {
                  selectedGeofication = selectedGeofication!!.copy(delay = it.roundToInt())
                  Vibrate.vibrate(context, 15)
                  inputValid = isInputValid()
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
                processEdit(
                  context,
                  selectedGeofence!!,
                  selectedGeofication!!
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
