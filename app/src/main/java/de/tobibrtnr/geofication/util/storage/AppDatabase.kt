package de.tobibrtnr.geofication.util.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceDao
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import de.tobibrtnr.geofication.util.storage.geofication.GeoficationDao
import de.tobibrtnr.geofication.util.storage.log.LogDao
import de.tobibrtnr.geofication.util.storage.log.LogEntry
import de.tobibrtnr.geofication.util.storage.setting.Setting
import de.tobibrtnr.geofication.util.storage.setting.SettingsDao

@Database(
  entities = [Geofence::class, Geofication::class, LogEntry::class, Setting::class],
  version = 1
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun geofenceDao(): GeofenceDao
  abstract fun geoficationDao(): GeoficationDao
  abstract fun logDao(): LogDao
  abstract fun settingsDao(): SettingsDao
}
