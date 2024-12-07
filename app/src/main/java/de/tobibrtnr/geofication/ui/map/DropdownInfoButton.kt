package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.tobibrtnr.geofication.R

// A button that is next to the search bar and opens a
// dropdown with links to the faq and feedback screens.
@Composable
fun DropdownInfoButton(
  navController: NavHostController,
  rmFocus: () -> Unit
) {
  val th = 56

  var showMenu by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier.shadow(
      elevation = 16.dp,
      shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
    )
  ) {
    Row(
      Modifier
        .width((0.75 * th).dp)
        .height(th.dp)
        .background(
          TextFieldDefaults.colors().focusedContainerColor,
          RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
        )
    ) {
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
          contentDescription = stringResource(R.string.open_dropdown_menu)
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
          text = { Text(stringResource(R.string.about_help_faq)) }
        )
        DropdownMenuItem(
          onClick = {
            showMenu = false
            navController.navigate("support")
          },
          text = { Text(stringResource(R.string.support)) }
        )
      }
    }
  }
}
