package de.tobibrtnr.geofication.util.storage.log

import android.util.Log
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogUtil {
  companion object {

    // Get all existing log entries
    suspend fun getLogs(dispatcher: CoroutineDispatcher = Dispatchers.IO): List<LogEntry> {
      return withContext(dispatcher) {
        val db = ServiceProvider.database()
        val logDao = db.logDao()
        logDao.getAll()
      }
    }

    // Add a new log entry
    fun addLog(message: String, severity: Int = 0) {
      val db = ServiceProvider.database()
      val logDao = db.logDao()

      val entry =
        LogEntry(message = message, severity = severity, timestamp = System.currentTimeMillis())

      CoroutineScope(SupervisorJob()).launch {
        Log.d("Geofication", message)
        logDao.insertAll(entry)
      }
    }

    // Delete all log entries
    suspend fun deleteAll(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
      withContext(dispatcher) {
        val db = ServiceProvider.database()
        val logDao = db.logDao()
        logDao.deleteAll()
      }
    }
  }
}