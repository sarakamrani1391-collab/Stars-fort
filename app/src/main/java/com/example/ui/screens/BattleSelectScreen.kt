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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SportsMartialArts
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BattleStage
import com.example.model.DifficultyLevel
import com.example.model.TroopType
import com.example.ui.components.CartoonTroopBadge
import com.example.ui.components.ChunkyGameButton
import com.example.ui.components.ResourceCostRow
import com.example.ui.components.ResourceTopHud
import com.example.ui.components.StarRatingRow
import com.example.ui.theme.GameColors
import com.example.ui.theme.LocalBiomePalette
import com.example.ui.theme.biomePalette
import com.example.ui.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleSelectScreen(
  uiState: GameUiState,
  onStartBattle: (BattleStage, Map<TroopType, Int>) -> Unit,
  onGoToBarracks: () -> Unit,
) {
  val palette = LocalBiomePalette.current
  var pickedStage by remember { mutableStateOf<BattleStage?>(null) }
  val squadDraft = remember { mutableStateMapOf<TroopType, Int>() }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(palette.skyTop, GameColors.DeepNavy))),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(modifier = Modifier.widthIn(max = 760.dp).fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
      ResourceTopHud(player = uiState.player, storageCap = uiState.storageCap)
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "نقشه نبردهای استراتژیک",
          style = MaterialTheme.typography.headlineMedium,
          color = GameColors.GoldYellow,
        )
        Surface(color = GameColors.MidnightNavy, shape = RoundedCornerShape(12.dp)) {
          Text(
            text = "کل ستاره‌ها: ${uiState.player.totalStars}/۳۶",
            style = MaterialTheme.typography.labelLarge,
            color = GameColors.GoldYellow,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(uiState.stages, key = { it.id }) { stage ->
          val bPal = biomePalette(stage.biome)
          val diffColor =
            when (stage.difficulty) {
              DifficultyLevel.EASY -> GameColors.EmeraldGreen
              DifficultyLevel.MEDIUM -> GameColors.SkyBlue
              DifficultyLevel.HARD -> GameColors.OrangeBright
              DifficultyLevel.LEGENDARY -> GameColors.RoyalPurple
            }

          Card(
            modifier = Modifier.fillMaxWidth().testTag("stage_card_${stage.id}"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
            border = BorderStroke(1.5.dp, if (stage.unlocked) bPal.primary.copy(alpha = 0.6f) else GameColors.SoftBorder),
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "مرحله ${stage.id}: ${stage.titleFa}",
                    style = MaterialTheme.typography.titleMedium,
                    color = GameColors.PureWhite,
                  )
                  Text(
                    text = "${stage.biome.titleFa} • سختی: ${stage.difficulty.titleFa}",
                    style = MaterialTheme.typography.labelSmall,
                    color = diffColor,
                  )
                }
                if (stage.unlocked) {
                  StarRatingRow(stars = stage.starsEarned)
                } else {
                  Icon(Icons.Rounded.Lock, contentDescription = "قفل", tint = GameColors.SoftBorder)
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = stage.descriptionFa,
                style = MaterialTheme.typography.bodyMedium,
                color = GameColors.OffWhite.copy(alpha = 0.85f),
              )
              Spacer(modifier = Modifier.height(8.dp))
              ResourceCostRow(cost = stage.reward)
              Spacer(modifier = Modifier.height(10.dp))

              ChunkyGameButton(
                text = if (stage.unlocked) "انتخاب نیرو و حمله" else "ابتدا مرحله قبل را فتح کن",
                enabled = stage.unlocked,
                icon = if (stage.unlocked) Icons.Rounded.SportsMartialArts else Icons.Rounded.Lock,
                containerColor = bPal.primary,
                onClick = {
                  squadDraft.clear()
                  uiState.troops.forEach { t ->
                    if (t.count > 0) squadDraft[t.type] = t.count
                  }
                  pickedStage = stage
                },
                modifier = Modifier.fillMaxWidth().testTag("attack_stage_${stage.id}"),
              )
            }
          }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
      }
    }

    pickedStage?.let { stage ->
      ModalBottomSheet(
        onDismissRequest = { pickedStage = null },
        containerColor = GameColors.MidnightNavy,
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
          Text(
            text = "چینش ارتش برای ${stage.titleFa}",
            style = MaterialTheme.typography.headlineMedium,
            color = GameColors.GoldYellow,
          )
          Text(
            text = "قدرت پیشنهادی: ${stage.recommendedPower} • قدرت ارتش شما: ${uiState.armyPower}",
            style = MaterialTheme.typography.bodyMedium,
            color = GameColors.SkyBlue,
          )
          Spacer(modifier = Modifier.height(12.dp))

          val availableTroops = uiState.troops.filter { it.count > 0 }
          if (availableTroops.isEmpty()) {
            Text(
              text = "هنوز هیچ نیروی آماده‌ای در پادگان نداری! ابتدا از بخش «نیروها و فناوری» سرباز آموزش بده.",
              style = MaterialTheme.typography.bodyLarge,
              color = GameColors.CoralRed,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ChunkyGameButton(
              text = "رفتن به پادگان",
              containerColor = GameColors.SkyBlue,
              onClick = {
                pickedStage = null
                onGoToBarracks()
              },
              modifier = Modifier.fillMaxWidth(),
            )
          } else {
            availableTroops.forEach { troop ->
              val chosen = squadDraft[troop.type] ?: 0
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CartoonTroopBadge(type = troop.type, size = 42.dp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(troop.type.titleFa, style = MaterialTheme.typography.titleMedium, color = GameColors.PureWhite)
                    Text("موجودی: ${troop.count}", style = MaterialTheme.typography.labelSmall, color = GameColors.Turquoise)
                  }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(
                    onClick = { if (chosen > 0) squadDraft[troop.type] = chosen - 1 },
                  ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "کاهش", tint = GameColors.CoralRed)
                  }
                  Text(
                    text = "$chosen",
                    style = MaterialTheme.typography.titleLarge,
                    color = GameColors.GoldYellow,
                    modifier = Modifier.padding(horizontal = 8.dp),
                  )
                  IconButton(
                    onClick = { if (chosen < troop.count) squadDraft[troop.type] = chosen + 1 },
                  ) {
                    Icon(Icons.Rounded.Add, contentDescription = "افزایش", tint = GameColors.EmeraldGreen)
                  }
                }
              }
            }
            Spacer(modifier = Modifier.height(14.dp))
            ChunkyGameButton(
              text = "شروع نبرد!",
              icon = Icons.Rounded.PlayArrow,
              containerColor = GameColors.OrangeBright,
              enabled = squadDraft.values.sum() > 0,
              onClick = {
                val chosenMap = squadDraft.toMap()
                pickedStage = null
                onStartBattle(stage, chosenMap)
              },
              modifier = Modifier.fillMaxWidth().testTag("confirm_start_battle_button"),
            )
          }
          Spacer(modifier = Modifier.height(22.dp))
        }
      }
    }
  }
}
