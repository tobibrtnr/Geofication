package de.tobibrtnr.geofication.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun CircleWithColor(modifier: Modifier = Modifier, color: Color, radius: Dp) {
  Box(
    modifier = modifier
      .size(radius * 2)
      .clip(CircleShape)
      .background(color)
  )
}
