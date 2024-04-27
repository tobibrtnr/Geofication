package de.tobibrtnr.geofication.ui.geofications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.Geofication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GeoficationsViewModel : ViewModel() {
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
          _geoficationsArray.update { list }
        }
    }
  }
}
