package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun StartupTutorialScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .zIndex(100f),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        style = MaterialTheme.typography.bodyLarge.copy(
          shadow = Shadow(color = Color.Black, blurRadius = 16f)
        ),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        text = "Hold down on the map and drag\nto create your first Geofication!",
        color = Color(237, 237, 237)

      )

      Spacer(Modifier.height(8.dp))

      Box(
        contentAlignment = Alignment.Center
      ) {
        Icon(
          modifier = Modifier.size(70.dp).blur(8.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
          imageVector = Icons.Filled.SwipeDown,
          contentDescription = "Icon Shadow",
          tint = Color.DarkGray
        )
        Icon(
          modifier = Modifier.size(60.dp),
          imageVector = Icons.Filled.SwipeDown,
          contentDescription = "Touch here",
          tint = Color(237, 237, 237)
        )
      }
    }
  }
}