package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.ChunkyGameButton
import com.example.ui.components.ResourceCostRow
import com.example.ui.components.ResourceTopHud
import com.example.ui.theme.GameColors
import com.example.ui.theme.LocalBiomePalette
import com.example.ui.viewmodel.GameUiState

@Composable
fun MissionsScreen(
  uiState: GameUiState,
  onClaimMission: (String) -> Unit,
  onClaimAchievement: (String) -> Unit,
) {
  val palette = LocalBiomePalette.current
  var selectedTab by remember { mutableIntStateOf(0) }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(palette.skyTop, GameColors.DeepNavy))),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(modifier = Modifier.widthIn(max = 760.dp).fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
      ResourceTopHud(player = uiState.player, storageCap = uiState.storageCap)
      Spacer(modifier = Modifier.height(10.dp))

      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = GameColors.MidnightNavy,
        contentColor = GameColors.GoldYellow,
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("مأموریت‌های روزانه", style = MaterialTheme.typography.labelLarge) },
          modifier = Modifier.testTag("tab_daily_missions"),
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("دستاوردها", style = MaterialTheme.typography.labelLarge) },
          modifier = Modifier.testTag("tab_achievements"),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(uiState.missions, key = { it.id }) { m ->
            val progressRatio = (m.progress.toFloat() / m.target.coerceAtLeast(1)).coerceIn(0f, 1f)
            Card(
              modifier = Modifier.fillMaxWidth().testTag("mission_card_${m.id}"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
              border = BorderStroke(1.5.dp, if (m.isComplete && !m.claimed) GameColors.GoldYellow else GameColors.SoftBorder),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Rounded.TaskAlt, contentDescription = null, tint = GameColors.EmeraldGreen)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(m.titleFa, style = MaterialTheme.typography.titleMedium, color = GameColors.PureWhite)
                    Text(m.descriptionFa, style = MaterialTheme.typography.bodyMedium, color = GameColors.OffWhite.copy(alpha = 0.8f))
                  }
                  Text("${m.progress}/${m.target}", style = MaterialTheme.typography.labelLarge, color = GameColors.GoldYellow)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                  progress = { progressRatio },
                  modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                  color = GameColors.EmeraldGreen,
                  trackColor = GameColors.DeepNavy,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ResourceCostRow(cost = m.reward)
                Spacer(modifier = Modifier.height(10.dp))
                ChunkyGameButton(
                  text = if (m.claimed) "پاداش دریافت شد ✓" else if (m.isComplete) "دریافت پاداش (+${m.rewardXp} XP)" else "در حال انجام",
                  enabled = m.isComplete && !m.claimed,
                  icon = Icons.Rounded.CardGiftcard,
                  containerColor = GameColors.GoldYellow,
                  onClick = { onClaimMission(m.id) },
                  modifier = Modifier.fillMaxWidth().testTag("claim_mission_${m.id}"),
                )
              }
            }
          }
          item { Spacer(modifier = Modifier.height(16.dp)) }
        }
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(uiState.achievements, key = { it.id }) { a ->
            val progressRatio = (a.progress.toFloat() / a.target.coerceAtLeast(1)).coerceIn(0f, 1f)
            Card(
              modifier = Modifier.fillMaxWidth().testTag("achievement_card_${a.id}"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
              border = BorderStroke(1.5.dp, if (a.isComplete && !a.claimed) GameColors.RoyalPurple else GameColors.SoftBorder),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = GameColors.GoldYellow)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(a.titleFa, style = MaterialTheme.typography.titleMedium, color = GameColors.PureWhite)
                    Text(a.descriptionFa, style = MaterialTheme.typography.bodyMedium, color = GameColors.OffWhite.copy(alpha = 0.8f))
                  }
                  Text("${a.progress}/${a.target}", style = MaterialTheme.typography.labelLarge, color = GameColors.Turquoise)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                  progress = { progressRatio },
                  modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                  color = GameColors.RoyalPurple,
                  trackColor = GameColors.DeepNavy,
                )
                Spacer(modifier = Modifier.height(10.dp))
                ChunkyGameButton(
                  text = if (a.claimed) "باز شده ✓" else if (a.isComplete) "دریافت ${a.rewardCrystal} کریستال" else "پاداش: ${a.rewardCrystal} کریستال",
                  enabled = a.isComplete && !a.claimed,
                  containerColor = GameColors.RoyalPurple,
                  contentColor = GameColors.PureWhite,
                  onClick = { onClaimAchievement(a.id) },
                  modifier = Modifier.fillMaxWidth().testTag("claim_achievement_${a.id}"),
                )
              }
            }
          }
          item { Spacer(modifier = Modifier.height(16.dp)) }
        }
      }
    }
  }
}
