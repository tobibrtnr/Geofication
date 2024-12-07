package de.tobibrtnr.geofication.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.google.android.gms.maps.model.LatLng
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.util.storage.log.LogUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
fun LocationSearchBar(
  modifier: Modifier = Modifier,
  input: MutableState<String>,
  callback: (LatLng) -> Unit,
  clearFocus: () -> Unit
) {
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  var locationName by input

  // Custom text field with placeholders, transparency, icons and custom keyboard actions
  TextField(
    value = locationName,
    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search_location)) },
    onValueChange = { locationName = it },
    placeholder = {
      Text(
        text = stringResource(R.string.search_location_or_geofication),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    modifier = modifier,
    colors = TextFieldDefaults.colors(
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent
    ),
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Search
    ),
    keyboardActions = KeyboardActions(
      onSearch = {
        keyboardController?.hide()
        focusManager.clearFocus()
        searchLocation(locationName, context, callback, clearFocus)
      }
    ),
    trailingIcon = {
      if(locationName.isNotEmpty()) {
        IconButton(onClick = {
          locationName = ""
        }) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.clear_search_query)
          )
        }
      }
    }
  )
}

// Function to search for a location globally.
fun searchLocation(
  locString: String,
  context: Context,
  callback: (LatLng) -> Unit,
  clearFocus: () -> Unit
) {
  if (locString.isNotEmpty()) {

    searchGlobally(context, locString, {
      handleAddresses(it, callback)
    }, {
      showNoResultsToast(context, clearFocus)
    })
  }
}

// Function to show a Toast when no global result has been found.
private fun showNoResultsToast(context: Context, clearFocus: () -> Unit) {
  MainScope().launch {
    Toast.makeText(context, context.getString(R.string.no_global_location), Toast.LENGTH_SHORT)
      .show()
  }
  clearFocus()
}

// Function to receive a position from a list of addresses
fun handleAddresses(addresses: List<Address>, callback: (LatLng) -> Unit) {
  if (addresses.isNotEmpty()) {
    val address = addresses[0]
    val latLng = LatLng(address.latitude, address.longitude)

    callback(latLng)
  }
}

// Function to search for a location name globally.
// Custom callbacks if a result has been found or not.
fun searchGlobally(
  context: Context,
  query: String,
  resultFoundCallback: (List<Address>) -> Unit,
  noResultFoundCallback: () -> Unit = {}
) {
  val geocoder = Geocoder(context)

  // Use different implementations for finding a
  // location globally, depending on the Android version.
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // New implementation of GeocodeListener
    val listener = object : GeocodeListener {
      override fun onGeocode(p0: MutableList<Address>) {
        if (p0.isEmpty()) {
          noResultFoundCallback()
        } else {
          resultFoundCallback(p0)
        }
      }

      // Log any error messages
      override fun onError(errorMessage: String?) {
        errorMessage?.let {
          LogUtil.addLog(errorMessage)
        }
      }
    }
    geocoder.getFromLocationName(query, 1, listener)
  } else {
    // Geocoder implementation for older versions of Android
    @Suppress("DEPRECATION")
    val addresses = geocoder.getFromLocationName(query, 1)
    if (addresses != null) {
      resultFoundCallback(addresses)
    } else {
      noResultFoundCallback()
    }
  }
}
