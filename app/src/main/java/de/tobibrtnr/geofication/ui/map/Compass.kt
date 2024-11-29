package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.CameraPositionState

@Composable
fun Compass(modifier: Modifier, cameraPositionState: CameraPositionState, onPress: () -> Unit) {

  val angle by remember {
    derivedStateOf { cameraPositionState.position.bearing }
  }

  // Use default Material Theme colors for the compass
  val bgColor = MaterialTheme.colorScheme.primaryContainer
  val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha=0.12f)

  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(bgColor)
      .border(BorderStroke(1.dp, borderColor), shape = CircleShape)
      .clickable { onPress() },
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height
      val radius = size.minDimension / 2
      val paint = Paint()


      // Rotate the whole canvas to simulate the compass rotation
      rotate(angle) {
        // Draw the north red triangle with shadow
        paint.color = Color(0xFFDB4242)

        drawIntoCanvas { canvas ->
          val path = Path().apply {
            moveTo(canvasWidth / 2, 10f)
            lineTo(canvasWidth / 2 - 12f, canvasHeight / 2)
            lineTo(canvasWidth / 2 + 12f, canvasHeight / 2)
            close()
          }
          canvas.drawPath(path, paint)
        }

        // Draw the south dark gray triangle with shadow
        paint.color = Color(0xFF6E6E6E)

        drawIntoCanvas { canvas ->
          val path = Path().apply {
            moveTo(canvasWidth / 2, canvasHeight - 10)
            lineTo(canvasWidth / 2 - 12f, canvasHeight / 2)
            lineTo(canvasWidth / 2 + 12f, canvasHeight / 2)
            close()
          }
          canvas.drawPath(path, paint)
        }
      }

      // Draw the inner grey circle in the middle
      drawCircle(
        color = bgColor,
        radius = radius / 10,
        center = Offset(x = canvasWidth / 2, y = canvasHeight / 2)
      )
    }
  }
}
