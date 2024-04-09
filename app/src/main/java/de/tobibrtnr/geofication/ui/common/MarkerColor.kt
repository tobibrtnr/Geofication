package de.tobibrtnr.geofication.ui.common

import androidx.compose.ui.graphics.Color

// Enum that holds possible Geofication category colors and
// the corresponding hue to color the map marker.
enum class MarkerColor(val color: Color, val hue: Float) {
  RED(Color(0xFFEA3535), 0f),
  ORANGE(Color(0xFFEA9035), 30f),
  YELLOW(Color(0xFFEAEA35), 60f),
  GREEN(Color(0xFF35EA35), 120f),
  CYAN(Color(0xFF35EAEA), 180f),
  AZURE(Color(0xFF3590EA), 210f),
  BLUE(Color(0xFF3535EA), 240f),
  VIOLET(Color(0xFF9035EA), 270f),
  MAGENTA(Color(0xFFEA35EA), 300f),
  ROSE(Color(0xFFEA3590), 330f),
}
