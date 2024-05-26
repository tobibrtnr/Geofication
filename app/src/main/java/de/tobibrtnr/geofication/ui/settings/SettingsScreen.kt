package de.tobibrtnr.geofication.ui.settings

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.util.storage.LogEntry
import de.tobibrtnr.geofication.util.storage.LogUtil
import de.tobibrtnr.geofication.util.storage.SettingsUtil
import de.tobibrtnr.geofication.util.storage.UnitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
) {

  val context = LocalContext.current
  val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

  var selectedMode by remember { mutableStateOf(SettingsUtil.getThemeMode()) }

  // true is meter, false is foot
  var selectedUnit by remember { mutableStateOf(UnitUtil.getDistanceUnit()) }

  var logEntryArray by remember { mutableStateOf(emptyList<LogEntry>()) }
  LaunchedEffect(Unit) {
    val logEntries = LogUtil.getLogs()
    logEntryArray = logEntries
  }

  // UI
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Text(text = stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(R.string.theme), style = MaterialTheme.typography.titleLarge)
    Row(verticalAlignment = Alignment.CenterVertically) {
      RadioButton(
        selected = selectedMode == "yes",
        onClick = {
          selectedMode = "yes"; SettingsUtil.setThemeMode("yes"); uiModeManager.setApplicationNightMode(
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
          selectedMode = "no"; SettingsUtil.setThemeMode("no"); uiModeManager.setApplicationNightMode(
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
          selectedMode = "auto"; SettingsUtil.setThemeMode("auto"); uiModeManager.setApplicationNightMode(
          UiModeManager.MODE_NIGHT_AUTO
        )
        },
      )
      Text(text = stringResource(R.string.system_default))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(R.string.distance_unit), style = MaterialTheme.typography.titleLarge)
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
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(text = stringResource(R.string.debug_log), style = MaterialTheme.typography.titleLarge)
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

    LazyColumn {
      items(logEntryArray) {
        ListItem(it)
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
