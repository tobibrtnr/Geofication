package de.tobibrtnr.geofication.util.misc

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class Vibrate {
  companion object {
    // Start a vibration of a given length
    fun vibrate(context: Context, ms: Long) {
      val vibrator = getVibrate(context)

      // Check if the device has vibration capabilities
      if (vibrator.hasVibrator()) {
        val vibrationEffect = VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
      }
    }

    // Start a vibrate pattern with given length of vibration
    fun startVibratePattern(context: Context, ms: Long) {
      val vibrator = getVibrate(context)

      // Vibrate for x ms, pause for x ms
      val pattern = longArrayOf(0, ms, ms)
      vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    // Cancel vibration pattern
    fun cancelVibration(context: Context) {
      val vibrator = getVibrate(context)
      vibrator.cancel()
    }

    // Get vibrator service depending on SDK version
    private fun getVibrate(context: Context): Vibrator {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
          context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      }
    }
  }
}
