package de.tobibrtnr.geofication

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.maps.MapsInitializer
import de.tobibrtnr.geofication.ui.GeoficationApp
import de.tobibrtnr.geofication.ui.theme.GeoficationTheme
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.LocaleUtil
import de.tobibrtnr.geofication.util.storage.UnitUtil

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()

    super.onCreate(savedInstanceState)

    var intentQuery = ""
    val uri: Uri? = intent.data
    uri?.let {
      if(uri.scheme == "geo") {
        val query = uri.query
        val ssp = uri.schemeSpecificPart
        if(query != null && query.startsWith("q=")) {
          intentQuery = query.substring(2).split("(")[0]
        } else if (ssp != null) {
          intentQuery = ssp.split("?")[0]
        }
      }
    }

    MapsInitializer.initialize(this)
    ServiceProvider.setInstance(this)

    UnitUtil.init(this)
    LocaleUtil.init(this)

    val openGeoId = intent.getIntExtra("openGeoId", -1)

    setContent {
      GeoficationTheme {
        GeoficationApp(openGeoId = openGeoId, intentQuery = intentQuery)
      }
    }
  }
}
