package de.tobibrtnr.geofication.util.receivers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.getByteInput
import de.tobibrtnr.geofication.util.misc.sendNotification
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import de.tobibrtnr.geofication.util.storage.LogUtil

class GeoficationWorker(context: Context, workerParams: WorkerParameters) :
  CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    ServiceProvider.setInstance(applicationContext)

    val tFence = getByteInput(inputData.getByteArray("tFence")) as Geofence
    val tNotif = getByteInput(inputData.getByteArray("tNotif")) as Geofication

    val geofenceTransition = inputData.getInt("geofenceTransition", 0)

    // If the flags equals the triggered one or is both, and the geofication is active:
    if ((tNotif.flags == geofenceTransition || tNotif.flags == 3) && tNotif.active) {
      GeofenceUtil.incrementNotifTriggerCount(tNotif.id)
    }

    when (tNotif.onTrigger) {
      1 -> GeofenceUtil.setNotifActive(tNotif.id, false)
      2 -> GeofenceUtil.deleteGeofence(tFence.id)
    }

    LogUtil.addLog("Attempt to send Notification \"${tNotif.message}\", \"${tFence.fenceName}\"")

    sendNotification(
      applicationContext,
      tFence,
      tNotif
    )

    return Result.success()
  }
}
