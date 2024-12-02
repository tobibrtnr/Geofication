package de.tobibrtnr.geofication.ui.settings

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.BuildConfig
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.DeleteAllConfirmPopup
import de.tobibrtnr.geofication.ui.common.ResetSettingsPopup
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.setting.LocaleUtil
import de.tobibrtnr.geofication.util.storage.setting.UnitUtil
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.log.LogEntry
import de.tobibrtnr.geofication.util.storage.log.LogUtil
import de.tobibrtnr.geofication.util.storage.setting.SettingsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  innerPadding: PaddingValues,
  geofenceViewModel: GeofenceViewModel
) {
  val context = LocalContext.current
  val activity = context as? ComponentActivity

  val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

  var selectedMode by remember { mutableStateOf(SettingsUtil.getThemeMode()) }

  var powerPopup by remember { mutableStateOf(SettingsUtil.getPowerPopup()) }

  // true is meter, false is foot
  var selectedUnit by remember { mutableStateOf(UnitUtil.getDistanceUnit()) }

  // locale
  val languageNames by lazy { context.resources.getStringArray(R.array.languages) }
  val languageCodes by lazy { context.resources.getStringArray(R.array.language_codes) }
  var selectedLocale by remember { mutableStateOf(LocaleUtil.getLocale()) }

  var deleteAllPopupVisible by remember { mutableStateOf(false) }
  var resetSettingsPopupVisible by remember { mutableStateOf(false) }

  var logEntryArray by remember { mutableStateOf(emptyList<LogEntry>()) }

  val versionPrefix = if(BuildConfig.DEBUG) {
    "d"
  } else {
    "v"
  }

  LaunchedEffect(Unit) {
    val logEntries = LogUtil.getLogs()
    logEntryArray = logEntries
  }

  // Delete all Geofications Popup
  if (deleteAllPopupVisible) {
    DeleteAllConfirmPopup(
      onConfirm = {
        deleteAllPopupVisible = false
        geofenceViewModel.deleteAllGeofences()
      },
      onCancel = { deleteAllPopupVisible = false }
    )
  }

  // Reset Settings Popup
  if (resetSettingsPopupVisible) {
    ResetSettingsPopup(
      onConfirm = {
        resetSettingsPopupVisible = false
        CoroutineScope(Dispatchers.Default).launch {
          Locale.setDefault(context.applicationContext.resources.configuration.locales[0])

          SettingsUtil.resetSettings()

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(
              UiModeManager.MODE_NIGHT_AUTO
            )
          }
          activity?.restartApp()
        }
      },
      onCancel = { resetSettingsPopupVisible = false }
    )
  }

  // UI
  Scaffold(
    topBar = {
      TopAppBar(title = { Text(stringResource(R.string.settings)) }
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .padding(
          top = paddingValues.calculateTopPadding(),
          start = 16.dp,
          end = 16.dp,
          bottom = innerPadding.calculateBottomPadding()
        )
    ) {
      Column(Modifier.verticalScroll(rememberScrollState())) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Dark / Light Theme
        Text(text = stringResource(R.string.theme), style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(
            selected = selectedMode == "yes",
            onClick = {
              selectedMode =
                "yes"; SettingsUtil.setThemeMode("yes"); uiModeManager.setApplicationNightMode(
              UiModeManager.MODE_NIGHT_YES
            )
            },
          )
          Text(text = stringResource(R.string.dark_mode))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(
            selected = selectedMode == "no",
            onClick = {
              selectedMode =
                "no"; SettingsUtil.setThemeMode("no"); uiModeManager.setApplicationNightMode(
              UiModeManager.MODE_NIGHT_NO
            )
            },
          )
          Text(text = stringResource(R.string.light_mode))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(
            selected = selectedMode == "auto",
            onClick = {
              selectedMode =
                "auto"; SettingsUtil.setThemeMode("auto"); uiModeManager.setApplicationNightMode(
              UiModeManager.MODE_NIGHT_AUTO
            )
            },
          )
          Text(text = stringResource(R.string.system_default))
        }

        Spacer(modifier = Modifier.height(8.dp))
      }
        // Distance Unit
        Text(
          text = stringResource(R.string.distance_unit),
          style = MaterialTheme.typography.titleLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(
            selected = selectedUnit,
            onClick = {
              selectedUnit = true
              UnitUtil.setDistanceUnit(true)
            },
          )
          Text(text = stringResource(R.string.metric_m))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(
            selected = !selectedUnit,
            onClick = {
              selectedUnit = false
              UnitUtil.setDistanceUnit(false)
            },
          )
          Text(text = stringResource(R.string.imperial_ft))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Locale
        Text(text = stringResource(R.string.language), style = MaterialTheme.typography.titleLarge)

        languageNames.forEachIndexed { index, language ->
          val isSelected = selectedLocale == languageCodes[index]
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .selectable(
                selected = isSelected,
                onClick = {
                  selectedLocale = languageCodes[index]
                  LocaleUtil.setLocale(languageCodes[index])

                  activity?.restartApp()
                },
                role = Role.RadioButton
              )
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = isSelected,
              onClick = null // null recommended for accessibility with screen readers
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = language)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Miscellaneous
        Text(
          text = stringResource(R.string.miscellaneous),
          style = MaterialTheme.typography.titleLarge
        )

        Row(
          modifier = Modifier.fillMaxWidth()
        ) {
          Switch(checked = powerPopup, onCheckedChange = {
            Vibrate.vibrate(context, 50)
            powerPopup = !powerPopup; SettingsUtil.setPowerPopup(powerPopup)
          })
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = stringResource(R.string.show_popup_at_startup_if))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
          deleteAllPopupVisible = true
        }) {
          Text(text = stringResource(R.string.delete_all_geofications_cd))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
          resetSettingsPopupVisible = true
        }) {
          Text(text = stringResource(R.string.reset_settings))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(
            R.string.geofication_version,
            versionPrefix,
            BuildConfig.VERSION_NAME
          ),
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Debug Log, show only in debug mode
        if (BuildConfig.DEBUG) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stringResource(R.string.debug_log),
              style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
              CoroutineScope(SupervisorJob()).launch {
                LogUtil.deleteAll()
                logEntryArray = LogUtil.getLogs()
              }
            }) {
              Text(stringResource(R.string.clear_log))
            }
          }
          Spacer(modifier = Modifier.height(8.dp))

          LazyColumn(Modifier.height(500.dp)) {
            items(logEntryArray) {
              ListItem(it)
            }

          }
        }
      }
    }
  }
}

@Composable
fun ListItem(logEntry: LogEntry) {
  Card(
    colors = CardDefaults.cardColors()
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(logEntry.timestamp))

        Text(
          text = formattedDate,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 1
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(logEntry.message)
    }
  }
  Spacer(modifier = Modifier.height(8.dp))
}

fun ComponentActivity.restartApp() {
  val intent = Intent(this, this::class.java)
  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
  startActivity(intent)
  finish()
}
