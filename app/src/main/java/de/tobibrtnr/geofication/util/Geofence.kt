package de.tobibrtnr.geofication.util

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class Geofence(
  @PrimaryKey val gid: String,
  @ColumnInfo(name = "latitude") val latitude: Double,
  @ColumnInfo(name = "longitude") val longitude: Double,
  @ColumnInfo(name = "radius") val radius: Float
)

@Dao
interface GeofenceDao {
  @Query("SELECT * FROM geofence")
  fun getAll(): List<Geofence>

  @Query("SELECT * FROM geofence WHERE gid LIKE :geoId")
  fun loadById(geoId: String): Geofence

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg geofences: Geofence)

  @Delete
  fun delete(geofence: Geofence)
}

@Database(entities = [Geofence::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun geofenceDao(): GeofenceDao
}
