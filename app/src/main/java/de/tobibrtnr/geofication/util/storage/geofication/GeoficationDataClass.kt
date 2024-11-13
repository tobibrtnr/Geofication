package de.tobibrtnr.geofication.util.storage.geofication

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import java.io.Serializable

@Entity(
  foreignKeys = [ForeignKey(
    entity = Geofence::class,
    parentColumns = ["id"],
    childColumns = ["fenceid"],
    onDelete = ForeignKey.CASCADE
  )]
)
data class Geofication(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  @ColumnInfo(index = true) var fenceid: Int,

  var message: String,
  var flags: Int,
  var delay: Int,
  val repeat: Boolean,
  val active: Boolean,
  var onTrigger: Int,
  var link: String,
  var isAlarm: Boolean,

  val triggerCount: Int,
  val created: Long,
  var lastEdit: Long
) : Serializable
