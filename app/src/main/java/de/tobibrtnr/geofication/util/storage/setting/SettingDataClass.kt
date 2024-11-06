package de.tobibrtnr.geofication.util.storage.setting

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Setting(
  @PrimaryKey
  val key: String,
  val value: ByteArray
) {

  // Generated
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Setting

    if (key != other.key) return false

    return true
  }

  override fun hashCode(): Int {
    return key.hashCode()
  }
}
