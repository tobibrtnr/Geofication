package de.tobibrtnr.geofication.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.Geofication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state#4

/*data class MapState(
  val geofencesArray : List<Geofence> = emptyList(),
  val geoficationsArray: List<Geofication> = emptyList()
)*/

class MapViewModel : ViewModel() {
  /*private val _uiState = MutableStateFlow(MapState())
  val uiState: StateFlow<MapState> = _uiState.asStateFlow()*/

  private val _geofencesArray = MutableStateFlow(emptyList<Geofence>())
  val geofencesArray = _geofencesArray.asStateFlow()

  private val _geoficationsArray = MutableStateFlow(emptyList<Geofication>())
  val geoficationsArray = _geoficationsArray.asStateFlow()

  init {
    getData()
  }

  private fun getData() {
    val database = ServiceProvider.database()
    viewModelScope.launch {
      database.geoficationDao().getAllFlow().flowOn(Dispatchers.Default)
        .collect { list ->
          println("$list \n\n\n\n\n\n")
          _geoficationsArray.update { list }
        }
    }
    viewModelScope.launch {
      database.geofenceDao().getAllFlow().flowOn(Dispatchers.IO)
        .collect { list ->
          println("$list \n\n\n\n\n\n")
          _geofencesArray.update { list }
        }
    }
  }
}