package de.tobibrtnr.geofication.ui.infos

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavHostController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.tobibrtnr.geofication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(navController: NavHostController) {

  val context = LocalContext.current

  var showMenu by remember { mutableStateOf(false)}

  val faqList = listOf(
    stringResource(R.string.faq_1_q) to stringResource(R.string.faq_1_a),
    stringResource(R.string.faq_4_q) to stringResource(R.string.faq_4_a),
    stringResource(R.string.faq_5_q) to stringResource(R.string.faq_5_a),
    stringResource(R.string.faq_2_q) to stringResource(R.string.faq_2_a),
    stringResource(R.string.faq_3_q) to stringResource(R.string.faq_3_a)
  )

  Scaffold(
    topBar = {
      TopAppBar(title = { Text(stringResource(R.string.about_help_faq)) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back)
            )
          }
        },
        actions = {
          IconButton(onClick = { showMenu = true}) {
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
                OssLicensesMenuActivity.setActivityTitle(getString(context, R.string.open_source_licenses))
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
              },
              text = { Text(stringResource(R.string.open_source_licenses)) }
            )
          }
        }
      )
    }
  ) {
    Box(
      modifier = Modifier
        .padding(top = it.calculateTopPadding(), start = 16.dp, end = 16.dp)
    ) {
      Column {
        //Text(
        //  text = stringResource(R.string.further_information),
        //  modifier = Modifier.padding(bottom = 16.dp)
        //)

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            //.padding(vertical = 16.dp)
        ) {
          items(faqList.size) { index ->
            FaqItem(question = faqList[index].first, answer = faqList[index].second)
          }
        }
      }
    }
  }
}

@Composable
fun FaqItem(question: String, answer: String) {
  var isExpanded by remember { mutableStateOf(false) }
  val rotationAngle by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    animationSpec = tween(durationMillis = 300), label = ""
  )

  Card(
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clickable { isExpanded = !isExpanded }
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = question,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
            .rotate(rotationAngle)
        )
      }
      AnimatedVisibility(visible = isExpanded) {
        Text(
          text = answer,
          fontSize = 16.sp,
          modifier = Modifier.padding(top = 8.dp)
        )
      }
    }
  }
}
