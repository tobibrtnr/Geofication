import de.tobibrtnr.geofication.util.storage.log.LogDao
import de.tobibrtnr.geofication.util.storage.log.LogEntry

class LogRepository(private val logDao: LogDao) {
  suspend fun getAll(): List<LogEntry> = logDao.getAll()

  suspend fun insertAll(vararg logEntries: LogEntry) = logDao.insertAll(*logEntries)

  suspend fun deleteAll() = logDao.deleteAll()
}