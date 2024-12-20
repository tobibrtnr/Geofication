package de.tobibrtnr.geofication.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

// A custom composable that displays a button with three
// options, exactly one of the options has to be set.
@Composable
fun SegmentedRadioButtons(opt1: String, opt2: String, opt3: String, onValueChange: (Int) -> Unit, onTrigger: Int = 1) {
  var selectedOption by remember { mutableIntStateOf(onTrigger) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
  ) {
    SegmentedRadioButton(
      modifier = Modifier.weight(1/3f),
      option = 0,
      label = opt1,
      selectedOption = selectedOption,
      onOptionSelected = {
        selectedOption = it
        onValueChange(selectedOption)
      },
      shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50),
    )
    SegmentedRadioButton(
      modifier = Modifier.weight(1/3f),
      option = 1,
      label = opt2,
      selectedOption = selectedOption,
      onOptionSelected = {
        selectedOption = it
        onValueChange(selectedOption)
      },
      shape = RectangleShape,
    )
    SegmentedRadioButton(
      modifier = Modifier.weight(1/3f),
      option = 2,
      label = opt3,
      selectedOption = selectedOption,
      onOptionSelected = {
        selectedOption = it
        onValueChange(selectedOption)
      },
      shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50),
    )
  }
}

// One button part of the segmented radio buttons composable.
@Composable
fun SegmentedRadioButton(
  modifier: Modifier,
  option: Int,
  label: String,
  selectedOption: Int,
  onOptionSelected: (Int) -> Unit,
  shape: Shape
) {
  val backgroundColor = if (selectedOption == option) {
    MaterialTheme.colorScheme.primary
  } else {
    Color.Gray
  }

  Button(
    modifier = modifier,
    onClick = { onOptionSelected(option) },
    colors = ButtonDefaults.buttonColors(
      backgroundColor
    ),
    shape = shape,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun SegmentedRadioButtonsPreview() {
  SegmentedRadioButtons(
    opt1 = "do nothing",
    opt2 = "deactivate",
    opt3 = "deactivate",
    {}
  )
}
