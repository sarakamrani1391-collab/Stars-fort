package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.model.TutorialStep
import com.example.ui.canvas.IsometricBaseCanvas
import com.example.ui.components.CartoonBuildingIcon
import com.example.ui.components.ChunkyGameButton
import com.example.ui.components.ResourceCostRow
import com.example.ui.components.ResourceTopHud
import com.example.ui.components.TutorialCommanderBanner
import com.example.ui.theme.GameColors
import com.example.ui.theme.LocalBiomePalette
import com.example.ui.viewmodel.GameUiState

@Composable
fun MainMapScreen(
  uiState: GameUiState,
  selectedBuildingId: Long?,
  movingBuildingId: Long?,
  selectedEmptyCell: Pair<Int, Int>?,
  placingBuildingType: BuildingType?,
  onGridTap: (Int, Int) -> Unit,
  onCollectAll: () -> Unit,
  onCollectBuilding: (Long) -> Unit,
  onUpgradeBuilding: (Long) -> Unit,
  onPlaceBuildingAt: (BuildingType, Int, Int) -> Unit,
  onStartMoveBuilding: (Long) -> Unit,
  onCancelMove: () -> Unit,
  onCancelPlace: () -> Unit,
  onDismissEmptyCell: () -> Unit,
  onDismissSelected: () -> Unit,
  onStartDefenseTest: () -> Unit,
  onOpenRealms: () -> Unit,
  onOpenSettings: () -> Unit,
  onSkipTutorial: () -> Unit,
  onNavigateForTutorial: (TutorialStep) -> Unit,
) {
  val palette = LocalBiomePalette.current
  val selectedBuilding = uiState.buildings.firstOrNull { it.id == selectedBuildingId }

  Box(modifier = Modifier.fillMaxSize()) {
    // Full-screen interactive 2.5D Isometric Village Canvas
    IsometricBaseCanvas(
      biome = uiState.player.activeBiome,
      buildings = uiState.buildings,
      selectedBuildingId = selectedBuildingId,
      movingBuildingId = movingBuildingId,
      selectedEmptyCell = selectedEmptyCell,
      isPlacingMode = placingBuildingType != null,
      animationsEnabled = uiState.preferences.animationsEnabled,
      onGridTap = onGridTap,
      modifier = Modifier.fillMaxSize().testTag("isometric_base_canvas"),
    )

    // Top HUD + Quick Map Controls
    Column(
      modifier =
        Modifier.align(Alignment.TopCenter)
          .widthIn(max = 760.dp)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      ResourceTopHud(player = uiState.player, storageCap = uiState.storageCap)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
            onClick = onOpenRealms,
            color = GameColors.MidnightNavy.copy(alpha = 0.9f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.6f)),
            modifier = Modifier.testTag("map_realms_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Rounded.Public, contentDescription = "قلمروها", tint = palette.secondary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("قلمروها", style = MaterialTheme.typography.labelLarge, color = GameColors.PureWhite)
            }
          }

          Surface(
            onClick = onStartDefenseTest,
            color = GameColors.MidnightNavy.copy(alpha = 0.9f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GameColors.OrangeBright.copy(alpha = 0.7f)),
            modifier = Modifier.testTag("map_defense_test_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Rounded.Shield, contentDescription = "تست دفاع", tint = GameColors.OrangeBright, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("آزمون دفاع", style = MaterialTheme.typography.labelLarge, color = GameColors.PureWhite)
            }
          }
        }

        Surface(
          onClick = onOpenSettings,
          color = GameColors.MidnightNavy.copy(alpha = 0.9f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, GameColors.SoftBorder),
          modifier = Modifier.testTag("map_settings_button"),
        ) {
          Box(modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Rounded.Settings, contentDescription = "تنظیمات", tint = GameColors.PureWhite, modifier = Modifier.size(20.dp))
          }
        }
      }

      TutorialCommanderBanner(
        step = uiState.preferences.tutorialStep,
        onSkip = onSkipTutorial,
        onActionClick = { onNavigateForTutorial(uiState.preferences.tutorialStep) },
      )
    }

    // Bottom floating controls (Move banner, Collect All, or Selected Building Inspector)
    Column(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .widthIn(max = 760.dp)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AnimatedVisibility(visible = movingBuildingId != null) {
        Surface(
          color = GameColors.Turquoise,
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "حالت جابه‌جایی: یک خانه سبز خالی را روی نقشه لمس کن",
              style = MaterialTheme.typography.labelLarge,
              color = GameColors.DeepNavy,
              modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onCancelMove) {
              Icon(Icons.Rounded.Close, contentDescription = "لغو جابه‌جایی", tint = GameColors.DeepNavy)
            }
          }
        }
      }

      AnimatedVisibility(visible = selectedBuilding != null) {
        selectedBuilding?.let { b ->
          val upgradeCost = GameCatalog.buildingUpgradeCost(b.type, b.level)
          val stored = b.storedGold + b.storedWood + b.storedStone + b.storedCrystal
          Card(
            modifier = Modifier.fillMaxWidth().testTag("selected_building_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy.copy(alpha = 0.96f)),
            border = BorderStroke(2.dp, GameColors.GoldYellow),
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CartoonBuildingIcon(type = b.type, size = 46.dp)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "${b.type.titleFa} (سطح ${b.level}/${b.type.maxLevel})",
                      style = MaterialTheme.typography.titleMedium,
                      color = GameColors.PureWhite,
                    )
                    Text(
                      text = b.type.descriptionFa,
                      style = MaterialTheme.typography.labelSmall,
                      color = GameColors.OffWhite.copy(alpha = 0.75f),
                    )
                  }
                }
                IconButton(onClick = onDismissSelected) {
                  Icon(Icons.Rounded.Close, contentDescription = "بستن", tint = GameColors.OffWhite)
                }
              }

              if (b.level < b.type.maxLevel) {
                Spacer(modifier = Modifier.height(8.dp))
                ResourceCostRow(cost = upgradeCost, available = uiState.player.resources)
              }

              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                if (b.level < b.type.maxLevel) {
                  ChunkyGameButton(
                    text = "ارتقا",
                    icon = Icons.Rounded.ArrowUpward,
                    containerColor = GameColors.EmeraldGreen,
                    onClick = { onUpgradeBuilding(b.id) },
                    modifier = Modifier.weight(1f).testTag("inspect_upgrade_button"),
                  )
                }
                ChunkyGameButton(
                  text = "جابه‌جایی",
                  icon = Icons.Rounded.OpenWith,
                  containerColor = GameColors.SkyBlue,
                  onClick = { onStartMoveBuilding(b.id) },
                  modifier = Modifier.weight(1f).testTag("inspect_move_button"),
                )
                if (stored > 0) {
                  ChunkyGameButton(
                    text = "برداشت ($stored)",
                    icon = Icons.Rounded.Savings,
                    containerColor = GameColors.GoldYellow,
                    onClick = { onCollectBuilding(b.id) },
                    modifier = Modifier.weight(1f).testTag("inspect_collect_button"),
                  )
                }
              }
            }
          }
        }
      }

      AnimatedVisibility(visible = placingBuildingType != null) {
        Surface(
          color = GameColors.EmeraldGreen,
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "ساخت ${placingBuildingType?.titleFa ?: ""}: خانه خالی مورد نظر را لمس کن",
              style = MaterialTheme.typography.labelLarge,
              color = GameColors.DeepNavy,
              modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onCancelPlace) {
              Icon(Icons.Rounded.Close, contentDescription = "لغو ساخت", tint = GameColors.DeepNavy)
            }
          }
        }
      }

      AnimatedVisibility(visible = selectedEmptyCell != null && selectedBuilding == null && movingBuildingId == null) {
        selectedEmptyCell?.let { (gx, gy) ->
          Card(
            modifier = Modifier.fillMaxWidth().testTag("empty_cell_place_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy.copy(alpha = 0.96f)),
            border = BorderStroke(2.dp, GameColors.Turquoise),
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "ساخت در مختصات (${gx + 1}, ${gy + 1})",
                  style = MaterialTheme.typography.titleMedium,
                  color = GameColors.Turquoise,
                )
                IconButton(onClick = onDismissEmptyCell) {
                  Icon(Icons.Rounded.Close, contentDescription = "بستن", tint = GameColors.OffWhite)
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                listOf(
                  BuildingType.CITIZEN_COTTAGE,
                  BuildingType.DEFENSE_TOWER,
                  BuildingType.PROTECTIVE_WALL,
                ).forEach { bType ->
                  ChunkyGameButton(
                    text = bType.titleFa,
                    containerColor = GameColors.GoldYellow,
                    onClick = { onPlaceBuildingAt(bType, gx, gy) },
                    modifier = Modifier.weight(1f).testTag("quick_place_${bType.name}"),
                  )
                }
              }
            }
          }
        }
      }

      if (selectedBuilding == null && movingBuildingId == null && selectedEmptyCell == null && placingBuildingType == null) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            color = GameColors.MidnightNavy.copy(alpha = 0.88f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GameColors.SoftBorder),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Rounded.Bolt, contentDescription = null, tint = GameColors.GoldYellow, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "دفاع: ${uiState.defensePower} • ارتش: ${uiState.armyHousingUsed}/${uiState.armyHousingCap}",
                style = MaterialTheme.typography.labelLarge,
                color = GameColors.PureWhite,
              )
            }
          }

          ChunkyGameButton(
            text = if (uiState.pendingCollectTotal > 0) "جمع‌آوری همه (+${uiState.pendingCollectTotal})" else "جمع‌آوری منابع",
            icon = Icons.Rounded.Savings,
            containerColor = if (uiState.pendingCollectTotal > 0) GameColors.GoldYellow else GameColors.SlateNavy,
            contentColor = if (uiState.pendingCollectTotal > 0) GameColors.DeepNavy else GameColors.OffWhite,
            onClick = onCollectAll,
            modifier = Modifier.testTag("collect_all_button"),
          )
        }
      }
    }
  }
}
