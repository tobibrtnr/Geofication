package de.tobibrtnr.geofication.util.storage

import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UnitUtil {
  companion object {

    private var currentUnit: Boolean = true

    fun init() {
      CoroutineScope(SupervisorJob()).launch {
        currentUnit = try {
          val db = ServiceProvider.database()
          val setDao = db.settingsDao()
          setDao.getSetting("unit")[0].toInt() != 0
        } catch (e: NullPointerException) {
          // Setting does not exist yet
          true
        }
      }
    }

    fun setDistanceUnit(value: Boolean) {
      currentUnit = value
      CoroutineScope(SupervisorJob()).launch {
        val db = ServiceProvider.database()
        val setDao = db.settingsDao()
        val unitSetting = Setting("unit", if (value) byteArrayOf(1) else byteArrayOf(0))
        setDao.setSetting(unitSetting)
      }
    }

    fun getDistanceUnit(): Boolean {
      return currentUnit
    }

    fun distanceUnit(): String {
      return if (currentUnit) {
        "m"
      } else {
        "ft"
      }
    }

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

    fun distanceFactor(): Double {
      return if (currentUnit) 1.0 else 3.2808398950131
    }
  }
}