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
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.model.TechType
import com.example.model.TroopType
import com.example.ui.components.CartoonTroopBadge
import com.example.ui.components.ChunkyGameButton
import com.example.ui.components.ResourceCostRow
import com.example.ui.components.ResourceTopHud
import com.example.ui.theme.GameColors
import com.example.ui.theme.LocalBiomePalette
import com.example.ui.viewmodel.GameUiState

@Composable
fun TroopsTechScreen(
  uiState: GameUiState,
  onTrainTroop: (TroopType, Int) -> Unit,
  onUpgradeTroop: (TroopType) -> Unit,
  onUpgradeTech: (TechType) -> Unit,
) {
  val palette = LocalBiomePalette.current
  var selectedTab by remember { mutableIntStateOf(0) }
  val citadelLevel =
    uiState.buildings.firstOrNull { it.type == BuildingType.COMMAND_CITADEL }?.level ?: 1
  val hasLab = uiState.buildings.any { it.type == BuildingType.TECH_LABORATORY }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(palette.skyTop, GameColors.DeepNavy))),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(modifier = Modifier.widthIn(max = 760.dp).fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
      ResourceTopHud(player = uiState.player, storageCap = uiState.storageCap)
      Spacer(modifier = Modifier.height(8.dp))

      Surface(
        color = GameColors.MidnightNavy,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GameColors.SoftBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "ظرفیت ارتش: ${uiState.armyHousingUsed}/${uiState.armyHousingCap}",
            style = MaterialTheme.typography.titleMedium,
            color = GameColors.SkyBlue,
          )
          Text(
            text = "قدرت کل: ${uiState.armyPower}",
            style = MaterialTheme.typography.titleMedium,
            color = GameColors.GoldYellow,
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = GameColors.MidnightNavy,
        contentColor = GameColors.GoldYellow,
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("پادگان و آموزش نیرو", style = MaterialTheme.typography.labelLarge) },
          modifier = Modifier.testTag("tab_barracks"),
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("درخت فناوری", style = MaterialTheme.typography.labelLarge) },
          modifier = Modifier.testTag("tab_tech_tree"),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(uiState.troops, key = { it.type.name }) { troop ->
            val def = GameCatalog.troops.getValue(troop.type)
            val unlocked = citadelLevel >= def.unlockCitadelLevel
            val upgradeCost = GameCatalog.troopUpgradeCost(troop.type, troop.level)
            val scaledHp = (def.baseHp * (1f + (troop.level - 1) * 0.22f)).toInt()
            val scaledAtk = (def.baseAttack * (1f + (troop.level - 1) * 0.22f)).toInt()

            Card(
              modifier = Modifier.fillMaxWidth().testTag("troop_card_${troop.type.name}"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
              border = BorderStroke(1.dp, if (unlocked) palette.primary.copy(alpha = 0.5f) else GameColors.SoftBorder),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CartoonTroopBadge(type = troop.type, size = 56.dp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = "${troop.type.titleFa} • سطح ${troop.level}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GameColors.PureWhite,
                      )
                      Surface(color = GameColors.Turquoise.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp)) {
                        Text(
                          text = "آماده: ${troop.count}",
                          style = MaterialTheme.typography.labelLarge,
                          color = GameColors.Turquoise,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                      }
                    }
                    Text(
                      text = troop.type.roleFa,
                      style = MaterialTheme.typography.bodyMedium,
                      color = GameColors.OffWhite.copy(alpha = 0.85f),
                    )
                    Text(
                      text = "جان: $scaledHp | حمله: $scaledAtk | فضا: ${def.housingSpace} | هدف: ${troop.type.targetPriorityFa}",
                      style = MaterialTheme.typography.labelSmall,
                      color = GameColors.GoldYellow,
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
                ResourceCostRow(cost = def.trainCost, available = uiState.player.resources)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  ChunkyGameButton(
                    text = if (unlocked) "آموزش ۱+" else "دژ سطح ${def.unlockCitadelLevel}",
                    enabled = unlocked,
                    icon = Icons.Rounded.Add,
                    containerColor = GameColors.EmeraldGreen,
                    onClick = { onTrainTroop(troop.type, 1) },
                    modifier = Modifier.weight(1f).testTag("train_troop_${troop.type.name}"),
                  )
                  ChunkyGameButton(
                    text = if (!hasLab) "نیاز به آزمایشگاه" else if (troop.level >= 5) "حداکثر" else "ارتقا (${upgradeCost.gold} طلا)",
                    enabled = unlocked && hasLab && troop.level < 5,
                    icon = Icons.Rounded.ArrowUpward,
                    containerColor = GameColors.RoyalPurple,
                    contentColor = GameColors.PureWhite,
                    onClick = { onUpgradeTroop(troop.type) },
                    modifier = Modifier.weight(1f).testTag("upgrade_troop_${troop.type.name}"),
                  )
                }
              }
            }
          }
          item { Spacer(modifier = Modifier.height(16.dp)) }
        }
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(uiState.techs, key = { it.type.name }) { tech ->
            val maxLvl = tech.type.maxLevel
            val cost = GameCatalog.techUpgradeCost(tech.type, tech.level)
            val affordable = uiState.player.resources.canAfford(cost)

            Card(
              modifier = Modifier.fillMaxWidth().testTag("tech_card_${tech.type.name}"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
              border = BorderStroke(1.dp, GameColors.RoyalPurple.copy(alpha = 0.55f)),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = GameColors.RoyalPurple.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(2.dp),
                  ) {
                    androidx.compose.material3.Icon(
                      imageVector = Icons.Rounded.Biotech,
                      contentDescription = tech.type.titleFa,
                      tint = GameColors.RoyalPurple,
                      modifier = Modifier.padding(10.dp),
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${tech.type.titleFa} • سطح ${tech.level}/$maxLvl",
                      style = MaterialTheme.typography.titleMedium,
                      color = GameColors.PureWhite,
                    )
                    Text(
                      text = tech.type.descriptionFa,
                      style = MaterialTheme.typography.bodyMedium,
                      color = GameColors.OffWhite.copy(alpha = 0.85f),
                    )
                  }
                }
                if (tech.level < maxLvl) {
                  Spacer(modifier = Modifier.height(8.dp))
                  ResourceCostRow(cost = cost, available = uiState.player.resources)
                }
                Spacer(modifier = Modifier.height(10.dp))
                ChunkyGameButton(
                  text = if (tech.level >= maxLvl) "فناوری تکمیل شده" else "پژوهش سطح ${tech.level + 1}",
                  enabled = tech.level < maxLvl && affordable,
                  containerColor = GameColors.Turquoise,
                  onClick = { onUpgradeTech(tech.type) },
                  modifier = Modifier.fillMaxWidth().testTag("research_tech_${tech.type.name}"),
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
