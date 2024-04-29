package de.tobibrtnr.geofication.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
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
    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search location") },
    onValueChange = { locationName = it },
    placeholder = {
      Text(
        text = "Search location or Geofication",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    modifier = modifier/*.clip(CircleShape)*/
      .border(
        1.dp,
        Color.LightGray,
        RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
      )
      .shadow(
        elevation = 16.dp,
        shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
      ),
    //.padding(3.dp)
    //textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
    shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50),
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
    )
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
    Toast.makeText(context, "No global location found for your given search query.", Toast.LENGTH_SHORT)
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
