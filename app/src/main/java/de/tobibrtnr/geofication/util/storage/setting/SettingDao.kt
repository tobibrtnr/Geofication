package de.tobibrtnr.geofication.util.storage.setting

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setSetting(setting: Setting)

  @Query("SELECT value FROM setting WHERE `key` = :name")
  suspend fun getSetting(name: String): ByteArray

  @Query("DELETE from setting")
  suspend fun resetSettings()
}
