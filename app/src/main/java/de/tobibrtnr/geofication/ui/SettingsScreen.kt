package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.util.LogEntry
import de.tobibrtnr.geofication.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
) {

  var logEntryArray by remember { mutableStateOf(emptyList<LogEntry>()) }
  LaunchedEffect(Unit) {
    val logEntries = LogUtil.getLogs()
    logEntryArray = logEntries
  }

  // UI
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = {
      CoroutineScope(SupervisorJob()).launch {
        LogUtil.deleteAll()
        logEntryArray = LogUtil.getLogs()

      }
    }) {
      Text("Clear Log")
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

