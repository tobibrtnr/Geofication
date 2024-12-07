package de.tobibrtnr.geofication.ui.startup

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

// This dummy PermissionState always returns granted and is used
// if the permission is not needed on older versions of Android.
@OptIn(ExperimentalPermissionsApi::class)
class DummyPermissionState(override val permission: String): PermissionState {
  override var status: PermissionStatus = PermissionStatus.Granted
  override fun launchPermissionRequest() {
    status = PermissionStatus.Granted
  }
}
