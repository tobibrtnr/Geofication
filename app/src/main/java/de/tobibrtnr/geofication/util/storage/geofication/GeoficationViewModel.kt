package de.tobibrtnr.geofication.util.storage.geofication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.GeoficationGeofence
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeoficationViewModel(
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

  // Internal data repository
  private val repository: GeoficationRepository

  // Flow to get a live state of the data
  val getAllFlow: StateFlow<List<Geofication>>

  // Initialize data repository and state flow
  init {
    val geoficationDao = ServiceProvider.database().geoficationDao()
    repository = GeoficationRepository(geoficationDao)
    getAllFlow = repository.getAllFlow()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  }

  // Delete a Geofication
  fun delete(gid: Int) {
    viewModelScope.launch {
      repository.delete(gid)
    }
  }

  // Increment the trigger count of a Geofication
  fun incrementTriggerCount(gid: Int) {
    viewModelScope.launch {
      repository.incrementTriggerCount(gid)
    }
  }

  // Set the active property of a given Geofication
  fun setActive(isActive: Boolean, gid: Int) {
    viewModelScope.launch {
      repository.setActive(isActive, gid)
    }
  }

  // All existing geofications
  @SuppressWarnings("kotlin:S6313")
  suspend fun getAll(): List<Geofication> {
    return withContext(dispatcher) {
      repository.getAll()
    }
  }

  // Search Geofications by their message or geofence name
  @SuppressWarnings("kotlin:S6313")
  suspend fun searchGeofications(query: String): List<GeoficationGeofence> {
    return withContext(dispatcher) {
      repository.searchGeofications(query)
    }
  }
}
