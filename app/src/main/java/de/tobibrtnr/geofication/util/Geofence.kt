package de.tobibrtnr.geofication.util

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class Geofence(
  @PrimaryKey val gid: String,
  val latitude: Double,
  val longitude: Double,
  val radius: Float,
  val color: String
)

@Entity(
  foreignKeys = [ForeignKey(
    entity = Geofence::class,
    parentColumns = ["gid"],
    childColumns = ["fenceid"],
    onDelete = ForeignKey.CASCADE
  )]
)
data class Geofication(
  @PrimaryKey val gid: String,
  @ColumnInfo(index = true) val fenceid: String,
  val message: String,
  val flags: Int,
  val delay: Int,
  val repeat: Boolean,
  val color: String
)

@Dao
interface GeofenceDao {
  @Query("SELECT * FROM geofence")
  fun getAll(): List<Geofence>

  @Query("SELECT * FROM geofence WHERE gid = :geoId")
  fun loadById(geoId: String): Geofence

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg geofences: Geofence)

  @Query("DELETE FROM geofence WHERE gid = :gid")
  fun delete(gid: String)
}

@Dao
interface GeoficationDao {
  @Query("SELECT * FROM geofication")
  fun getAll(): List<Geofication>

  @Query("SELECT * FROM geofication WHERE gid = :geoId")
  fun loadById(geoId: String): Geofication

  @Query("SELECT * FROM geofication WHERE fenceid = :fenceId")
  fun getByGeofence(fenceId: String): List<Geofication>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg geofications: Geofication)

  @Query("DELETE FROM geofication WHERE gid = :gid")
  fun delete(gid: String)
}

@Database(entities = [Geofence::class, Geofication::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun geofenceDao(): GeofenceDao
  abstract fun geoficationDao(): GeoficationDao
}
