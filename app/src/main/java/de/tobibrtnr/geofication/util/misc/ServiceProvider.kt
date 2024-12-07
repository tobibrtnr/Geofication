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

// Once initialized at the start of the app, this class allows
// us to access different services like storage or location
// without having to pass a context or check for initialization
class ServiceProvider private constructor(context: Context) {
  // Location client
  private var fusedLocationClient: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(context)

  // Geofencing client
  private var geofencingClient: GeofencingClient =
    LocationServices.getGeofencingClient(context)

  // Room database
  private var appDatabase: AppDatabase =
    Room.databaseBuilder(
      context,
      AppDatabase::class.java, "geofication-database"
    ).build()

  // ViewModels for Geofences and Geofications
  private lateinit var geofenceVM: GeofenceViewModel
  private lateinit var geoficationVM: GeoficationViewModel

  fun geofenceViewModelInitialized(): Boolean {
    return ::geofenceVM.isInitialized
  }

  fun geoficationViewModelInitialized(): Boolean {
    return ::geoficationVM.isInitialized
  }

  // This companion object allows as to access
  // the different services over static methods.
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
