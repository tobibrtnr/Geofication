package de.tobibrtnr.geofication.util.storage.setting

class SettingRepository(private val settingDao: SettingsDao) {
  suspend fun setSetting(setting: Setting) = settingDao.setSetting(setting)

  suspend fun getSetting(name: String): ByteArray = settingDao.getSetting(name)

  suspend fun resetSettings() = settingDao.resetSettings()
}
