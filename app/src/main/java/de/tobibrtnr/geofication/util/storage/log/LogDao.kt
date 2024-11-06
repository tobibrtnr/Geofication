package de.tobibrtnr.geofication.util.storage.log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LogDao {
  @Query("SELECT * FROM logEntry ORDER BY id DESC")
  suspend fun getAll(): List<LogEntry>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(vararg logEntries: LogEntry)

  @Query("DELETE FROM logEntry")
  suspend fun deleteAll()
}
