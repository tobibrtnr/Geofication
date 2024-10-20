package de.tobibrtnr.geofication.util.storage

import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SettingsUtil {
  companion object {

    private var themeMode: String = "auto"
    private var powerPopup: Boolean = true

    fun init() {
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        themeMode = try {
          String(setDao.getSetting("themeMode"))
        } catch (e: NullPointerException) {
          // Setting does not exist yet
          "auto"
        }

        powerPopup = try {
          val byteArray = setDao.getSetting("powerPopup")
          byteArray.isNotEmpty() && byteArray[0] == 1.toByte()
        } catch (e: NullPointerException) {
          // Setting does not exist yet
          true
        }
      }
    }

    fun getThemeMode(): String {
      return themeMode
    }

    fun setThemeMode(new: String) {
      themeMode = new;
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val themeSetting = Setting("themeMode", new.toByteArray())
        setDao.setSetting(themeSetting)
      }
    }

    fun getPowerPopup(): Boolean {
      return powerPopup
    }

    fun setPowerPopup(new: Boolean) {
      powerPopup = new;
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val themeSetting = Setting("powerPopup", byteArrayOf(if(new) 1 else 0))
        setDao.setSetting(themeSetting)
      }
    }

    fun resetSettings() {
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        setDao.resetSettings()
      }
    }
  }
}