package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun DropdownInfoButton(
  inputState: MutableState<String>,
  navController: NavHostController,
  rmFocus: () -> Unit
) {
  val th = 56
  var input by inputState

  var showMenu by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier.shadow(
      elevation = 16.dp,
      shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
    )
  ) {
    Row(
      Modifier
        .width(((if (input.isNotEmpty()) 1.27 else 0.75) * th).dp)
        .height(th.dp)
        .background(
          TextFieldDefaults.colors().focusedContainerColor,
          RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
        )
        .border(
          1.dp,
          Color.LightGray,
          RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
        )
        .clip(RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50))
    ) {
      if (input.isNotEmpty()) {
        Column(
          Modifier
            .weight(1f / 3f)
            .fillMaxHeight()
            .clickable {
              input = ""
            },
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Clear search query"
          )
        }
        VerticalDivider(thickness = 1.dp, color = Color.Black)
      }
      Column(
        Modifier
          .weight(2f / 3f)
          .fillMaxHeight()
          .clickable {
            showMenu = true
            rmFocus()
          },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Filled.MoreVert,
          contentDescription = "Open Dropdown Menu"
        )
      }

      DropdownMenu(
        offset = DpOffset(0.dp, 4.dp),
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
      ) {
        DropdownMenuItem(
          onClick = {
            showMenu = false
            navController.navigate("faq")
          },
          text = { Text("About, Help, FAQ") }
        )
        DropdownMenuItem(
          onClick = {
            showMenu = false
            println("TODO open Popup")
          },
          text = { Text("Support the Developer") }
        )
        DropdownMenuItem(
          onClick = {
            showMenu = false
            println("TODO open Popup")
          },
          text = { Text("Send Feedback") }
        )
      }
    }
  }
}