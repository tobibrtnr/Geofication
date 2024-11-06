package de.tobibrtnr.geofication.ui.startup

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
class DummyPermissionState(override val permission: String): PermissionState {

  override var status: PermissionStatus = PermissionStatus.Granted
  override fun launchPermissionRequest() {
    println("Start for $permission")
    status = PermissionStatus.Granted
  }
}
