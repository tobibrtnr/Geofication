package de.tobibrtnr.geofication.util.storage.geofication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.GeoficationGeofence
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeoficationViewModel(
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

  private val repository: GeoficationRepository

  val getAllFlow: StateFlow<List<Geofication>>

  init {
    val geoficationDao = ServiceProvider.database().geoficationDao()
    repository = GeoficationRepository(geoficationDao)
    getAllFlow = repository.getAllFlow()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  }

  suspend fun searchGeofications(query: String): List<GeoficationGeofence> {
    return withContext(dispatcher) {
      repository.searchGeofications(query)
    }
  }

  fun insertAll(vararg geofications: Geofication) {
    viewModelScope.launch {
      repository.insertAll(*geofications)
    }
  }

  fun delete(gid: Int) {
    viewModelScope.launch {
      repository.delete(gid)
    }
  }

  fun incrementTriggerCount(gid: Int) {
    viewModelScope.launch {
      repository.incrementTriggerCount(gid)
    }
  }

  fun setActive(isActive: Boolean, gid: Int) {
    viewModelScope.launch {
      repository.setActive(isActive, gid)
    }
  }

  fun deactivateAll(fenceid: Int) {
    viewModelScope.launch {
      repository.deactivateAll(fenceid)
    }
  }

  /**
   * Get current State of view model data
   */
  suspend fun getAll(): List<Geofication> {
    return withContext(dispatcher) {
      repository.getAll()
    }
  }
}