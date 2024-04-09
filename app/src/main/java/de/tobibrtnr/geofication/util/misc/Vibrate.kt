package de.tobibrtnr.geofication.util.misc

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class Vibrate {
  companion object {
    fun vibrate(context: Context, ms: Long) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

      // Check if the device has vibration capabilities
      if (vibrator.hasVibrator()) {
        // For API level 26 and above (Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val vibrationEffect = VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
          vibrator.vibrate(vibrationEffect)
        } else {
          // For devices below API level 26
          vibrator.vibrate(ms)
        }
      }
    }
  }
}