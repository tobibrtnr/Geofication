package de.tobibrtnr.geofication.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
fun SegmentedButtons(
  label1: String,
  label2: String,
  opt1: String,
  opt2: String,
  onValueChange: (List<String>) -> Unit,
  flags: Int = 1
) {
  val initialList = mutableListOf<String>()
  if (flags == 1 || flags == 3) {
    initialList.add(opt1)
  }
  if (flags == 2 || flags == 3) {
    initialList.add(opt2)
  }

  val selectedOptions by remember { mutableStateOf(initialList) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    SegmentedButton(
      label = label1,
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
      icon = Icons.AutoMirrored.Filled.ArrowForward
    )
    SegmentedButton(
      label = label2,
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
      icon = Icons.AutoMirrored.Filled.ArrowBack
    )
  }
}

@Composable
fun SegmentedButton(
  label: String,
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
        icon, contentDescription = label
      )
      Text(text = label)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun SegmentedButtonsPreview() {
  SegmentedButtons(
    label1 = "entering",
    label2 = "exiting",
    opt1 = "entering",
    opt2 = "exiting",
    onValueChange = {})
}