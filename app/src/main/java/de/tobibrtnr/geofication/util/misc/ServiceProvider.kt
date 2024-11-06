package de.tobibrtnr.geofication.util.misc

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import de.tobibrtnr.geofication.util.storage.AppDatabase
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.geofication.GeoficationViewModel
import java.lang.IllegalStateException

class ServiceProvider private constructor(context: Context) {
  private var fusedLocationClient: FusedLocationProviderClient
  private var geofencingClient: GeofencingClient
  private var appDatabase: AppDatabase

  private lateinit var geofenceVM: GeofenceViewModel
  private lateinit var geoficationVM: GeoficationViewModel

  init {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    geofencingClient = LocationServices.getGeofencingClient(context)
    appDatabase =
      Room.databaseBuilder(
        context,
        AppDatabase::class.java, "geofication-database"
      ).build()
  }

  fun geofenceViewModelInitialized(): Boolean {
    return ::geofenceVM.isInitialized
  }

  fun geoficationViewModelInitialized(): Boolean {
    return ::geoficationVM.isInitialized
  }

  companion object {
    @Volatile
    private var INSTANCE: ServiceProvider? = null

    fun setInstance(context: Context) {
      if(INSTANCE == null) {
        INSTANCE = ServiceProvider(context.applicationContext)
      }
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

    fun geofenceViewModel(): GeofenceViewModel {
      checkNotNull(INSTANCE) { "ServiceProvider must be initialized!" }
      if(!INSTANCE!!.geofenceViewModelInitialized()) {
        INSTANCE!!.geofenceVM = GeofenceViewModel()
      }
      return INSTANCE!!.geofenceVM
    }

    fun geoficationViewModel(): GeoficationViewModel {
      checkNotNull(INSTANCE) { "ServiceProvider must be initialized!" }
      if(!INSTANCE!!.geoficationViewModelInitialized()) {
        INSTANCE!!.geoficationVM = GeoficationViewModel()
      }
      return INSTANCE!!.geoficationVM
    }
  }
}
