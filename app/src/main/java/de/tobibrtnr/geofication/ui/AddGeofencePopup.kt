package de.tobibrtnr.geofication.ui

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import de.tobibrtnr.geofication.util.GeofenceUtil
import kotlinx.coroutines.Job

fun processInput(
  enteredString: String,
  enteredFloat: Float,
  enteredColor: String,
  pos: LatLng,
  context: Context,
  function: () -> Job
) {
  GeofenceUtil.addGeofence(
    context,
    enteredString,
    pos.latitude,
    pos.longitude,
    enteredFloat,
    enteredColor
  )

  function()
}

fun addGeofencePopup(pos: LatLng, context: Context, function: () -> Job) {
  // Create an AlertDialog.Builder
  val builder = AlertDialog.Builder(context)
  builder.setTitle("Add a new geofence:")

  // Create layout for EditTexts
  val layout = LinearLayout(context)
  layout.orientation = LinearLayout.VERTICAL

  // Create EditText for string input
  val stringInput = EditText(context)
  stringInput.hint = "Name"
  layout.addView(stringInput)

  // Create EditText for float input
  val floatInput = EditText(context)
  floatInput.hint = "Radius"
  layout.addView(floatInput)

  // Create EditText for float input
  val colorInput = EditText(context)
  colorInput.hint = "Color"
  layout.addView(colorInput)

  builder.setView(layout)

  // Set up the buttons
  builder.setPositiveButton("OK") { _, _ ->
    val enteredString = stringInput.text.toString()
    val enteredFloat = try {
      floatInput.text.toString().toFloat()
    } catch (e: NumberFormatException) {
      Toast.makeText(context, "Invalid radius input", Toast.LENGTH_SHORT).show()
      return@setPositiveButton
    }
    var enteredColor = colorInput.text.toString()
    if(enteredColor.isEmpty()) {
      enteredColor = "#FFFF0000"
    }

    // Process the entered values as needed
    processInput(enteredString, enteredFloat, enteredColor, pos, context, function)
  }

  builder.setNegativeButton("Cancel") { dialog, _ ->
    dialog.cancel()
  }

  // Create and show the AlertDialog
  val alertDialog = builder.create()
  alertDialog.show()
}