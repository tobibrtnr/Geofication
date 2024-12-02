package de.tobibrtnr.geofication.util.storage.setting

import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

class UnitUtil {
  companion object {

    // "true" is meter, "false" is foot
    private var currentUnit: Boolean = true

    // Initialize utility. If the setting does not exist yet,
    // check by locale if the current country uses m or ft.
    fun init() {
      CoroutineScope(SupervisorJob()).launch {
        currentUnit = try {
          val db = ServiceProvider.database()
          val setDao = db.settingsDao()
          setDao.getSetting("unit")[0].toInt() != 0
        } catch (e: NullPointerException) {
          val locale = Locale.getDefault()
          val imperialCountries = setOf("US", "LR", "MM")

          !imperialCountries.contains(locale.country)
        }
      }
    }

    // Set new distance unit
    fun setDistanceUnit(value: Boolean) {
      currentUnit = value
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val unitSetting = Setting("unit", if (value) byteArrayOf(1) else byteArrayOf(0))
        setDao.setSetting(unitSetting)
      }
    }

    // Get distance unit
    fun getDistanceUnit(): Boolean {
      return currentUnit
    }

    // Get distance unit specifier
    fun distanceUnit(): String {
      return if (currentUnit) {
        "m"
      } else {
        "ft"
      }
    }

    // Append the unit specifier to a given value in meters and
    // convert it to a bigger unit if suitable.
    fun appendUnit(meterValue: Int): String {
      return if (currentUnit) {
        if (meterValue >= 1000) {
          "${meterValue / 1000} km"
        } else {
          "${meterValue} m"
        }
      } else {
        val value = (3.2808398950131 * meterValue).toInt()
        if(value >= 5280) {
          "${value / 5280} mi"
        } else {
          "${value} ft"
        }
      }
    }

    // Get the distance factor to get the current unit from meters.
    fun distanceFactor(): Double {
      return if (currentUnit) 1.0 else 3.2808398950131
    }
  }
}
