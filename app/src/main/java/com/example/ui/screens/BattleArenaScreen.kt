package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BattleSimulationState
import com.example.model.TroopType
import com.example.ui.canvas.BattleArenaCanvas
import com.example.ui.components.CartoonTroopBadge
import com.example.ui.components.ChunkyGameButton
import com.example.ui.theme.GameColors

@Composable
fun BattleArenaScreen(
  battleState: BattleSimulationState,
  onSelectTroop: (TroopType) -> Unit,
  onDeployAt: (Float, Float) -> Unit,
  onDeployAll: () -> Unit,
  onExitBattle: () -> Unit,
) {
  BackHandler { onExitBattle() }

  Box(modifier = Modifier.fillMaxSize()) {
    BattleArenaCanvas(
      battleState = battleState,
      onDeployTap = onDeployAt,
      modifier = Modifier.fillMaxSize().testTag("battle_arena_canvas"),
    )

    // Top Battle HUD
    Surface(
      color = GameColors.MidnightNavy.copy(alpha = 0.9f),
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.5.dp, GameColors.GoldYellow.copy(alpha = 0.6f)),
      modifier =
        Modifier.align(Alignment.TopCenter)
          .safeDrawingPadding()
          .widthIn(max = 720.dp)
          .fillMaxWidth()
          .padding(12.dp),
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = battleState.stage?.titleFa ?: "نبرد",
              style = MaterialTheme.typography.titleMedium,
              color = GameColors.PureWhite,
            )
            Text(
              text =
                if (battleState.isDefenseTest) "دفاع خودکار برج‌های پایگاه شما"
                else "روی ناحیه آبی پایین نقشه بزن تا نیرو پیاده شود",
              style = MaterialTheme.typography.labelSmall,
              color = GameColors.SkyBlue,
            )
          }
          Surface(color = GameColors.DeepNavy, shape = RoundedCornerShape(10.dp)) {
            val remainingSec = (65 - battleState.elapsedSeconds.toInt()).coerceAtLeast(0)
            Text(
              text = "${remainingSec}ث",
              style = MaterialTheme.typography.titleMedium,
              color = GameColors.GoldYellow,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
          IconButton(onClick = onExitBattle, modifier = Modifier.testTag("exit_battle_button")) {
            Icon(Icons.Rounded.Close, contentDescription = "خروج از نبرد", tint = GameColors.CoralRed)
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "تخریب: ${battleState.destructionPercent}٪",
            style = MaterialTheme.typography.labelLarge,
            color = GameColors.EmeraldGreen,
          )
          Spacer(modifier = Modifier.width(10.dp))
          LinearProgressIndicator(
            progress = { battleState.destructionPercent / 100f },
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = GameColors.OrangeBright,
            trackColor = GameColors.DeepNavy,
          )
        }
      }
    }

    // Bottom Troop Deployment Bar (for campaign attack)
    if (!battleState.isDefenseTest) {
      Surface(
        color = GameColors.MidnightNavy.copy(alpha = 0.94f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.5.dp, GameColors.SoftBorder),
        modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 760.dp).fillMaxWidth(),
      ) {
        Column(modifier = Modifier.safeDrawingPadding().padding(12.dp)) {
          val remainingEntries = battleState.remainingToDeploy.entries.toList()
          if (remainingEntries.isNotEmpty()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
              ) {
                items(remainingEntries, key = { it.key.name }) { (type, count) ->
                  val isSelected = battleState.selectedDeployTroop == type
                  Surface(
                    color = if (isSelected) GameColors.Turquoise.copy(alpha = 0.25f) else GameColors.DeepNavy,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) GameColors.Turquoise else GameColors.SoftBorder),
                    modifier =
                      Modifier.clickable { onSelectTroop(type) }.testTag("deploy_chip_${type.name}"),
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                      CartoonTroopBadge(type = type, size = 38.dp)
                      Text(
                        text = "${type.titleFa} ($count)",
                        style = MaterialTheme.typography.labelSmall,
                        color = GameColors.PureWhite,
                      )
                    }
                  }
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              ChunkyGameButton(
                text = "اعزام همه",
                icon = Icons.Rounded.Bolt,
                containerColor = GameColors.GoldYellow,
                onClick = onDeployAll,
                modifier = Modifier.testTag("deploy_all_button"),
              )
            }
          } else {
            Text(
              text = "همه نیروها وارد نبرد شده‌اند! در حال نبرد...",
              style = MaterialTheme.typography.labelLarge,
              color = GameColors.GoldYellow,
              modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp),
            )
          }
        }
      }
    }
  }
}
