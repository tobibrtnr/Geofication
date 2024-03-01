package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedButtons(opt1: String, opt2: String, onValueChange: (List<String>) -> Unit) {
  var selectedOptions by remember { mutableStateOf(listOf("entering")) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    SegmentedButton(
      option = opt1,
      selectedOption = selectedOptions,
      onOptionSelected = {
        if (selectedOptions.contains(opt1)) {
          selectedOptions -= opt1
          if (!selectedOptions.contains(opt2)) {
            selectedOptions += opt2
          }
        } else {
          selectedOptions += opt1
        }
        onValueChange(selectedOptions)
      },
      shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50),
      icon = Icons.Filled.ArrowForward
    )
    SegmentedButton(
      option = opt2,
      selectedOption = selectedOptions,
      onOptionSelected = {
        if (selectedOptions.contains(opt2)) {
          selectedOptions -= opt2
          if (!selectedOptions.contains(opt1)) {
            selectedOptions += opt1
          }
        } else {
          selectedOptions += opt2
        }
        onValueChange(selectedOptions)
      },
      shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50),
      icon = Icons.Filled.ArrowBack
    )
  }
}

@Composable
fun SegmentedButton(
  option: String,
  selectedOption: List<String>,
  onOptionSelected: (String) -> Unit,
  shape: Shape,
  icon: ImageVector
) {
  val backgroundColor = if (selectedOption.contains(option)) {
    MaterialTheme.colorScheme.primary
  } else {
    Color.Gray
  }

  Button(
    onClick = { onOptionSelected(option) },
    colors = ButtonDefaults.buttonColors(
      backgroundColor
    ),
    shape = shape
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        icon, contentDescription = "button description"
      )
      Text(text = option)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun SegmentedButtonsPreview() {
  SegmentedButtons("entering", "exiting") {}
}