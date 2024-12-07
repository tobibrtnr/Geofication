package de.tobibrtnr.geofication.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.util.storage.log.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// Composable for a log entry item
@Composable
fun LogEntryItem(logEntry: LogEntry) {
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
