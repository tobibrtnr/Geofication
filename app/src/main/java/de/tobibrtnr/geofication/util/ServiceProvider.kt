package de.tobibrtnr.geofication.util

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import java.lang.IllegalStateException

class ServiceProvider private constructor(context: Context) {
  private var fusedLocationClient: FusedLocationProviderClient
  private var geofencingClient: GeofencingClient
  private var appDatabase: AppDatabase

  init {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    geofencingClient = LocationServices.getGeofencingClient(context)
    appDatabase =
      Room.databaseBuilder(
        context,
        AppDatabase::class.java, "geofication-database"
      ).build()
  }

  companion object {
    @Volatile
    private var INSTANCE: ServiceProvider? = null

    fun setInstance(context: Context) {
      INSTANCE = ServiceProvider(context.applicationContext)
    }

    fun location(): FusedLocationProviderClient {
      return INSTANCE?.fusedLocationClient
        ?: throw IllegalStateException("ServiceProvider must be initialized!")
    }

    fun geofence(): GeofencingClient {
      return INSTANCE?.geofencingClient
        ?: throw IllegalStateException("ServiceProvider must be initialized!")
    }

    fun database(): AppDatabase {
      return INSTANCE?.appDatabase
        ?: throw IllegalStateException("ServiceProvider must be initialized!")
    }

  }
}