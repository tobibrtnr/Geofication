package de.tobibrtnr.geofication.util.storage.setting

import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsUtil {
  companion object {

    private var themeMode: String = "auto"
    private var powerPopup: Boolean = true

    private var firstStartup: Boolean = true

    fun init() {
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()

        // Set theme mode setting
        themeMode = runBlocking {
          val byteArray = setDao.getSetting("themeMode")

          if (byteArray != null) {
            String(byteArray)
          } else {
            "auto"
          }
        }

        // Set power saving popup setting
        powerPopup = runBlocking {
          val byteArray = setDao.getSetting("powerPopup")

          if (byteArray != null) {
            byteArray.isNotEmpty() && byteArray[0] == 1.toByte()
          } else {
            true
          }
        }

        // Set if this is the first startup of the app
        firstStartup = runBlocking {
          val byteArray = setDao.getSetting("firstStartup")

          if (byteArray != null) {
            byteArray.isNotEmpty() && byteArray[0] == 1.toByte()
          } else {
            true
          }
        }
      }
    }

    // Get theme mode setting
    fun getThemeMode(): String {
      return themeMode
    }

    // Set theme mode setting
    fun setThemeMode(new: String) {
      themeMode = new
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val themeSetting = Setting("themeMode", new.toByteArray())
        setDao.setSetting(themeSetting)
      }
    }

    // Get power popup setting
    fun getPowerPopup(): Boolean {
      return powerPopup
    }

    // Set power popup setting
    fun setPowerPopup(new: Boolean) {
      powerPopup = new
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val themeSetting = Setting("powerPopup", byteArrayOf(if(new) 1 else 0))
        setDao.setSetting(themeSetting)
      }
    }

    // Get first startup setting
    fun getFirstStartup(): Boolean {
      return firstStartup
    }

    // Set first startup setting
    fun setFirstStartup(new: Boolean) {
      firstStartup = new
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val themeSetting = Setting("firstStartup", byteArrayOf(if(new) 1 else 0))
        setDao.setSetting(themeSetting)
      }
    }

    // Reset all settings
    fun resetSettings() {
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        setDao.resetSettings()
      }
    }

  }
}