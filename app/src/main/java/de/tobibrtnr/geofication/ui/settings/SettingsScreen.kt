package de.tobibrtnr.geofication.ui.settings

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
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
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.log.LogEntry
import de.tobibrtnr.geofication.util.storage.log.LogUtil
import de.tobibrtnr.geofication.util.storage.setting.LocaleUtil
import de.tobibrtnr.geofication.util.storage.setting.SettingsUtil
import de.tobibrtnr.geofication.util.storage.setting.UnitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  innerPadding: PaddingValues,
  geofenceViewModel: GeofenceViewModel
) {
  val context = LocalContext.current
  val activity = context as? ComponentActivity

  // Theme setting
  val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
  var selectedMode by remember { mutableStateOf(SettingsUtil.getThemeMode()) }

  // Setting if power saving popup is enabled
  var powerPopup by remember { mutableStateOf(SettingsUtil.getPowerPopup()) }

  // Unit setting
  // true is meter, false is foot
  var selectedUnit by remember { mutableStateOf(UnitUtil.getDistanceUnit()) }

  // Locale setting
  val languageNames by lazy { context.resources.getStringArray(R.array.languages) }
  val languageCodes by lazy { context.resources.getStringArray(R.array.language_codes) }
  var selectedLocale by remember { mutableStateOf(LocaleUtil.getLocale()) }

  // Popups for resetting settings and deleting all existing Geofications
  var deleteAllPopupVisible by remember { mutableStateOf(false) }
  var resetSettingsPopupVisible by remember { mutableStateOf(false) }

  // Log entries that will be shown in debug mode
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
        // Dark / Light Theme. Only available starting with Android S
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          Text(text = stringResource(R.string.theme), style = MaterialTheme.typography.titleLarge)

          RadioButtonRow(selectedMode == "yes", stringResource(R.string.dark_mode)) {
            selectedMode = "yes"
            SettingsUtil.setThemeMode("yes")
            uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
          }

          RadioButtonRow(selectedMode == "no", stringResource(R.string.light_mode)) {
            selectedMode = "no"
            SettingsUtil.setThemeMode("no")
            uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
          }

          RadioButtonRow(selectedMode == "auto", stringResource(R.string.system_default)) {
            selectedMode = "auto"
            SettingsUtil.setThemeMode("auto")
            uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
        // Distance unit setting
        Text(
          text = stringResource(R.string.distance_unit),
          style = MaterialTheme.typography.titleLarge
        )

        RadioButtonRow(selectedUnit, stringResource(R.string.metric_m)) {
          selectedUnit = true
          UnitUtil.setDistanceUnit(true)
        }

        RadioButtonRow(!selectedUnit, stringResource(R.string.imperial_ft)) {
          selectedUnit = false
          UnitUtil.setDistanceUnit(false)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Locale setting
        Text(text = stringResource(R.string.language), style = MaterialTheme.typography.titleLarge)

        languageNames.forEachIndexed { index, language ->
          val isSelected = selectedLocale == languageCodes[index]
          RadioButtonRow(isSelected, language) {
            selectedLocale = languageCodes[index]
            LocaleUtil.setLocale(languageCodes[index])

            activity?.restartApp()
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
              LogEntryItem(it)
            }
          }
        }
      }
    }
  }
}

// Composable for a row of a radio button setting
@Composable
fun RadioButtonRow(selected: Boolean, text: String, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(percent = 50))
      .selectable(
        selected = selected,
        onClick = {
          onClick()
        },
        role = Role.RadioButton
      )
      .padding(8.dp)
  ) {
    RadioButton(
      selected = selected,
      onClick = null // null recommended for accessibility with screen readers
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = text)
  }
}

// Extension function to restart the app
fun ComponentActivity.restartApp() {
  val intent = Intent(this, this::class.java)
  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
  startActivity(intent)
  finish()
}
