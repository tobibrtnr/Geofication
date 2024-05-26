package de.tobibrtnr.geofication.util.storage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import de.tobibrtnr.geofication.MainActivity
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.util.Locale

class LocaleUtil {
  companion object {

    // true is meter, false is foot
    private var currentLocale: String = ""

    fun init(context: Context) {
      CoroutineScope(SupervisorJob()).launch {
        currentLocale = try {
          val db = ServiceProvider.database()
          val setDao = db.settingsDao()
          val curLoc = setDao.getSetting("locale").toString(Charset.defaultCharset())
          setLocale(context, curLoc)
          curLoc
        } catch (e: NullPointerException) {
          // locale not set, use auto language or english as fallback.
          val languageCodes by lazy { context.resources.getStringArray(R.array.language_codes) }
          val localeStrings = (Locale.getDefault().language.split("[-_]+"));

          if (languageCodes.contains(localeStrings[0])) {
            setLocale(context, localeStrings[0])
            localeStrings[0]
          } else {
            setLocale(context, "en")
            "en"
          }

        }
      }
    }

    fun setLocale(context: Context, value: String) {
      currentLocale = value
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val localeSetting = Setting("locale", value.toByteArray(Charset.defaultCharset()))
        setDao.setSetting(localeSetting)
        setContextLocale(context, value)
      }
    }

    fun getLocale(): String {
      return currentLocale
    }

    private fun setContextLocale(context: Context, language: String) {
      context.resources.apply {
        val locale = Locale(language)
        val config = Configuration(configuration)

        context.createConfigurationContext(configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        // TODO use non deprecated method
        context.resources.updateConfiguration(config, displayMetrics)
      }
    }
  }
}