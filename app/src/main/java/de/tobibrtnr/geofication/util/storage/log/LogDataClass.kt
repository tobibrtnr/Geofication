package de.tobibrtnr.geofication.util.storage.log

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LogEntry(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  val timestamp: Long,
  val message: String,
  val severity: Int
)
