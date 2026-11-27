package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.BattleResultSummary
import com.example.ui.components.ChunkyGameButton
import com.example.ui.components.ResourceCostRow
import com.example.ui.components.StarRatingRow
import com.example.ui.theme.GameColors

@Composable
fun BattleResultScreen(
  result: BattleResultSummary?,
  onBackToMap: () -> Unit,
  onBackToBattles: () -> Unit,
) {
  BackHandler { onBackToMap() }

  val victory = result?.victory == true
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              if (victory) GameColors.EmeraldDark else GameColors.MidnightNavy,
              GameColors.DeepNavy,
            )
          )
        )
        .safeDrawingPadding(),
    contentAlignment = Alignment.Center,
  ) {
    Card(
      modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth().padding(20.dp),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
      border = BorderStroke(2.5.dp, if (victory) GameColors.GoldYellow else GameColors.CoralRed),
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text =
            when {
              result == null -> "پایان نبرد"
              result.isDefenseTest && victory -> "دفاع قهرمانانه موفق!"
              result.isDefenseTest -> "پایگاه نیاز به تقویت دارد!"
              victory -> "پیروزی درخشان!"
              else -> "تلاش خوب بود، دوباره آماده شو!"
            },
          style = MaterialTheme.typography.headlineLarge,
          color = if (victory) GameColors.GoldYellow else GameColors.OrangeBright,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = result?.stageTitleFa ?: "",
          style = MaterialTheme.typography.titleMedium,
          color = GameColors.SkyBlue,
        )

        Spacer(modifier = Modifier.height(14.dp))
        StarRatingRow(stars = result?.stars ?: 0, starSize = 40.dp)
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          color = GameColors.DeepNavy,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("درصد تخریب", style = MaterialTheme.typography.labelSmall, color = GameColors.OffWhite.copy(alpha = 0.7f))
              Text("${result?.destructionPercent ?: 0}٪", style = MaterialTheme.typography.titleLarge, color = GameColors.EmeraldGreen)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("امتیاز تجربه", style = MaterialTheme.typography.labelSmall, color = GameColors.OffWhite.copy(alpha = 0.7f))
              Text("+${result?.xpEarned ?: 0} XP", style = MaterialTheme.typography.titleLarge, color = GameColors.GoldYellow)
            }
            if (result?.isDefenseTest == false) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("نیروی بازگشته", style = MaterialTheme.typography.labelSmall, color = GameColors.OffWhite.copy(alpha = 0.7f))
                Text("${result.survivingTroops}/${result.totalDeployedTroops}", style = MaterialTheme.typography.titleLarge, color = GameColors.SkyBlue)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("غنائم و پاداش دریافتی:", style = MaterialTheme.typography.titleMedium, color = GameColors.PureWhite)
        Spacer(modifier = Modifier.height(8.dp))
        result?.let { ResourceCostRow(cost = it.loot) }

        Spacer(modifier = Modifier.height(22.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          ChunkyGameButton(
            text = "نقشه پایگاه",
            icon = Icons.Rounded.Home,
            containerColor = GameColors.EmeraldGreen,
            onClick = onBackToMap,
            modifier = Modifier.weight(1f).testTag("result_home_button"),
          )
          ChunkyGameButton(
            text = "نبردها",
            icon = Icons.Rounded.Replay,
            containerColor = GameColors.SkyBlue,
            onClick = onBackToBattles,
            modifier = Modifier.weight(1f).testTag("result_battles_button"),
          )
        }
      }
    }
  }
}
