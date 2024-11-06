package de.tobibrtnr.geofication.util.storage

import de.tobibrtnr.geofication.ui.common.MarkerColor

data class GeoficationGeofence(
  val message: String,
  val active: Boolean,

  val fenceName: String,
  val latitude: Double,
  val longitude: Double,
  var radius: Float,
  var color: MarkerColor,
)
