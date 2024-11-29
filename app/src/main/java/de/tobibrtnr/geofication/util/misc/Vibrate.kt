package de.tobibrtnr.geofication.util.misc

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class Vibrate {
  companion object {
    fun vibrate(context: Context, ms: Long) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

      // Check if the device has vibration capabilities
      if (vibrator.hasVibrator()) {
        val vibrationEffect = VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
      }
    }

    fun startVibratePattern(context: Context, ms: Long) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

      // Vibrate for x ms, pause for x ms
      val pattern = longArrayOf(0, ms, ms)

      // Check if device supports VibrationEffect for more control (API 26+)
      if (vibrator.hasAmplitudeControl()) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
      } else {
        vibrator.vibrate(pattern, 0)
      }
    }

    fun cancelVibration(context: Context) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.cancel()
    }
  }
}
