package de.tobibrtnr.geofication.ui

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.google.android.gms.maps.model.LatLng

@Composable
fun LocationSearchBar(modifier: Modifier = Modifier, callback: (LatLng) -> Unit) {

  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

  var locationName by remember { mutableStateOf("") }

  TextField(
    // on below line we are specifying value
    // for our message text field.
    value = locationName,
    // on below line we are adding on
    // value change for text field.
    onValueChange = { locationName = it },
    // on below line we are adding place holder
    // as text as "Enter your email"
    placeholder = { Text(text = "Enter your location to search") },
    // on below line we are adding modifier to it
    // and adding padding to it and filling max width
    modifier = modifier.clip(CircleShape),
    //.padding(3.dp)
    // on below line we are adding text style
    // specifying color and font size to it.
    //textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
    // on below line we are adding single line to it.
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Search
    ),
    keyboardActions = KeyboardActions(
      onSearch = {
        keyboardController?.hide()

        val loc = searchLocation(locationName, context, callback)
      }
    )
  )
}

private fun searchLocation(locString: String, context: Context, callback: (LatLng) -> Unit) {

  if (locString.isNotEmpty()) {
    val geocoder = Geocoder(context)

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      // Implementation of GeocodeListener
      val listener = object: GeocodeListener {
        override fun onGeocode(p0: MutableList<Address>) {
          handleAddresses(p0, callback)
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
      }
    }

  }
}

private fun handleAddresses(addresses: List<Address>, callback: (LatLng) -> Unit) {
  if(addresses.isNotEmpty()) {

    val address = addresses[0]
    val latLng = LatLng(address.latitude, address.longitude)

    callback(latLng)
  }
}
