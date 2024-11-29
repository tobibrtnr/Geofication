package de.tobibrtnr.geofication.util.storage.setting

import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
        themeMode = try {
          String(setDao.getSetting("themeMode"))
        } catch (e: NullPointerException) {
          // Default setting: Use device theme
          "auto"
        }

        // Set power saving popup setting
        powerPopup = try {
          val byteArray = setDao.getSetting("powerPopup")
          byteArray.isNotEmpty() && byteArray[0] == 1.toByte()
        } catch (e: NullPointerException) {
          // Default setting: Show popup on startup
          true
        }

        // Set if this is the first startup of the app
        firstStartup = try {
          val byteArray = setDao.getSetting("firstStartup")
          byteArray.isNotEmpty() && byteArray[0] == 1.toByte()
        } catch (e: NullPointerException) {
          // Default: yes
          true
        }
      }
    }

    // Get theme mode setting
    fun getThemeMode(): String {
      return themeMode
    }

    // Set theme mode setting
    fun setThemeMode(new: String) {
      themeMode = new;
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
      powerPopup = new;
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
      firstStartup = new;
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