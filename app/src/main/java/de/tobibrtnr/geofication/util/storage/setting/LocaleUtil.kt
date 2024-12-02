package de.tobibrtnr.geofication.util.storage.setting

import android.content.Context
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocaleUtil {
  companion object {

    // Currently set locale
    private var currentLocale: String = ""

    // Suppress "This key/object cannot ever be present in the collection"
    // at "if (languageCodes.contains(localeStrings[0])) {"
    @SuppressWarnings("kotlin:S2175")
    fun init(context: Context) {
      runBlocking {
        currentLocale = try {
          val db = ServiceProvider.database()
          val setDao = db.settingsDao()
          val curLoc = setDao.getSetting("locale").toString(Charset.defaultCharset())
          setLocale(curLoc)
          curLoc
        } catch (e: NullPointerException) {
          // Locale not set, use auto language or english as fallback.
          val languageCodes by lazy { context.resources.getStringArray(R.array.language_codes) }
          val localeStrings = Locale.getDefault().language.split("[-_]+")

          if (languageCodes.contains(localeStrings[0])) {
            setLocale(localeStrings[0])
            localeStrings[0]
          } else {
            // Default locale is english
            setLocale("en")
            "en"
          }
        }
      }
    }

    // Set a new locale setting
    fun setLocale(value: String) {
      currentLocale = value

      val db = ServiceProvider.database()
      val setDao = db.settingsDao()
      val localeSetting = Setting("locale", value.toByteArray(Charset.defaultCharset()))

      runBlocking {
        setDao.setSetting(localeSetting)
      }
    }

    // Get currently set locale
    fun getLocale(): String {
      return currentLocale
    }

    // Get date time by currently set locale
    fun getLocalDateTime(timestamp: Long, context: Context): String {
      val locale = Locale(currentLocale)
      val dateFormat = SimpleDateFormat(context.getString(R.string.date_time_format), locale)
      val date = Date(timestamp)

      return dateFormat.format(date)
    }
  }
}
