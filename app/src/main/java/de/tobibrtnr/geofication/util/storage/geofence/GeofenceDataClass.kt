package de.tobibrtnr.geofication.util.storage.geofence

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.tobibrtnr.geofication.ui.common.MarkerColor
import java.io.Serializable

@Entity
data class Geofence(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,

  var fenceName: String,
  val latitude: Double,
  val longitude: Double,
  var radius: Float,

  var color: MarkerColor,
  var active: Boolean,
  val triggerCount: Int,

  val created: Long,
  var lastEdit: Long
) : Serializable
