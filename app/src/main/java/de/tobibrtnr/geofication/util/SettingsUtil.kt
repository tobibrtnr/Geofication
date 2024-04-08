package de.tobibrtnr.geofication.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SettingsUtil {
  companion object {

    private var themeMode: String = "auto";

    fun init() {
      CoroutineScope(SupervisorJob()).launch {
        themeMode = try {
          val db = ServiceProvider.database()
          val setDao = db.settingsDao()
          setDao.getSetting("themeMode").toString()
        } catch (e: NullPointerException) {
          // Setting does not exist yet
          "auto"
        }
      }
    }

    fun getThemeMode(): String {
      return themeMode;
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
  }
}