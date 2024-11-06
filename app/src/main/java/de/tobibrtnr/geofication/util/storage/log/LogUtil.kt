package de.tobibrtnr.geofication.util.storage.log

import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogUtil {
  companion object {
    /**
     * Get all existing geofences.
     */
    suspend fun getLogs(): List<LogEntry> {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val logDao = db.logDao()
        logDao.getAll()
      }
    }

    fun addLog(message: String, severity: Int = 0) {
      val db = ServiceProvider.database()
      val logDao = db.logDao()

      val entry =
        LogEntry(message = message, severity = severity, timestamp = System.currentTimeMillis())

      CoroutineScope(SupervisorJob()).launch {
        logDao.insertAll(entry)
      }
    }

    suspend fun deleteAll() {
      withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val logDao = db.logDao()
        logDao.deleteAll()
      }
    }
  }
}