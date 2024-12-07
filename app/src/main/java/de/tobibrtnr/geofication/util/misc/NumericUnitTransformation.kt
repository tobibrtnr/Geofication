package de.tobibrtnr.geofication.util.misc

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import de.tobibrtnr.geofication.util.storage.setting.UnitUtil

// A VisualTransformation that allows us to append the currently
// set distance unit symbol to a number in a TextField.

@Composable
fun numericUnitTransformation() = VisualTransformation { text ->
  TransformedText(
    text = buildAnnotatedString {
      append(text)
      append(" ${UnitUtil.distanceUnit()}")
    },
    offsetMapping = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int {
        return offset
      }

      override fun transformedToOriginal(offset: Int): Int {
        return if (offset <= text.length) offset else text.length
      }
    }
  )
}
