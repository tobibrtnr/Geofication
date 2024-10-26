package de.tobibrtnr.geofication.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
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

fun searchLocation(
  locString: String,
  context: Context,
  callback: (LatLng) -> Unit,
  clearFocus: () -> Unit
) {

  if (locString.isNotEmpty()) {
    val geocoder = Geocoder(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      // Implementation of GeocodeListener
      val listener = object : GeocodeListener {
        override fun onGeocode(p0: MutableList<Address>) {
          if (p0.isEmpty()) {
            showNoResultsToast(context, clearFocus)
          } else {
            handleAddresses(p0, callback)
          }
        }

        override fun onError(errorMessage: String?) {
          println(errorMessage)
        }
      }
      geocoder.getFromLocationName(locString, 1, listener)
    } else {
      val addresses = geocoder.getFromLocationName(locString, 1)
      if (addresses != null) {
        handleAddresses(addresses, callback)
      } else {
        showNoResultsToast(context, clearFocus)
      }
    }

  }
}

private fun showNoResultsToast(context: Context, clearFocus: () -> Unit) {
  MainScope().launch {
    Toast.makeText(context, context.getString(R.string.no_global_location), Toast.LENGTH_SHORT)
      .show()
  }
  clearFocus()
}

fun handleAddresses(addresses: List<Address>, callback: (LatLng) -> Unit) {
  if (addresses.isNotEmpty()) {

    val address = addresses[0]
    val latLng = LatLng(address.latitude, address.longitude)

    callback(latLng)
  }
}
