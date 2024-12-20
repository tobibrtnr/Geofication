package de.tobibrtnr.geofication.ui.map

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.R

// A composable that displays a text on how to create
// a first Geofication, along with a moving icon.
@Composable
fun StartupTutorialScreen() {
  // Value to animate x movement of the pointer
  val offsetX by rememberInfiniteTransition(label = "pmx").animateValue(
    initialValue = 5.dp,
    targetValue = (-5).dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000),
      repeatMode = RepeatMode.Reverse
    ), label = "pointer_move_x"
  )

  // Value to animate y movement of the pointer
  val offsetY by rememberInfiniteTransition(label = "pmy").animateValue(
    initialValue = 0.dp,
    targetValue = 25.dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000),
      repeatMode = RepeatMode.Reverse
    ), label = "pointer_move_y"
  )

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        style = MaterialTheme.typography.bodyLarge.copy(
          shadow = Shadow(color = Color.Black, blurRadius = 16f)
        ),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        text = stringResource(R.string.tutorial_hold_down),
        color = Color(237, 237, 237)

      )

      Spacer(Modifier.height(8.dp))

      Box(
        modifier = Modifier.offset(y = offsetY, x = offsetX),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          modifier = Modifier
            .size(70.dp)
            .blur(8.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
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
