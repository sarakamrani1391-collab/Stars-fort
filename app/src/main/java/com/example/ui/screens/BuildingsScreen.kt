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
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.ui.components.CartoonBuildingIcon
import com.example.ui.components.ChunkyGameButton
import com.example.ui.components.ResourceCostRow
import com.example.ui.components.ResourceTopHud
import com.example.ui.theme.GameColors
import com.example.ui.theme.LocalBiomePalette
import com.example.ui.viewmodel.GameUiState

@Composable
fun BuildingsScreen(
  uiState: GameUiState,
  onBuildBuilding: (BuildingType) -> Unit,
  onUpgradeBuilding: (Long) -> Unit,
  onMoveOnMap: (Long) -> Unit,
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
          text = { Text("ساختمان‌های من (${uiState.buildings.size})", style = MaterialTheme.typography.labelLarge) },
          modifier = Modifier.testTag("tab_existing_buildings"),
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("ساخت بنای جدید", style = MaterialTheme.typography.labelLarge) },
          modifier = Modifier.testTag("tab_build_new"),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(uiState.buildings, key = { it.id }) { b ->
            val cost = GameCatalog.buildingUpgradeCost(b.type, b.level)
            val canUpgrade = b.level < b.type.maxLevel
            Card(
              modifier = Modifier.fillMaxWidth().testTag("building_item_${b.id}"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
              border = BorderStroke(1.dp, GameColors.SoftBorder),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CartoonBuildingIcon(type = b.type, size = 48.dp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${b.type.titleFa} • سطح ${b.level}/${b.type.maxLevel}",
                      style = MaterialTheme.typography.titleMedium,
                      color = GameColors.PureWhite,
                    )
                    Text(
                      text = "مختصات نقشه: (${b.gridX + 1}, ${b.gridY + 1})",
                      style = MaterialTheme.typography.labelSmall,
                      color = palette.secondary,
                    )
                  }
                }
                if (canUpgrade) {
                  Spacer(modifier = Modifier.height(8.dp))
                  ResourceCostRow(cost = cost, available = uiState.player.resources)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  ChunkyGameButton(
                    text = if (canUpgrade) "ارتقا به سطح ${b.level + 1}" else "حداکثر سطح",
                    enabled = canUpgrade,
                    icon = Icons.Rounded.ArrowUpward,
                    containerColor = GameColors.EmeraldGreen,
                    onClick = { onUpgradeBuilding(b.id) },
                    modifier = Modifier.weight(1f).testTag("upgrade_building_${b.id}"),
                  )
                  ChunkyGameButton(
                    text = "جابه‌جایی",
                    icon = Icons.Rounded.OpenWith,
                    containerColor = GameColors.SkyBlue,
                    onClick = { onMoveOnMap(b.id) },
                    modifier = Modifier.weight(1f).testTag("move_building_${b.id}"),
                  )
                }
              }
            }
          }
          item { Spacer(modifier = Modifier.height(16.dp)) }
        }
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          val buildable = BuildingType.entries.filter { it != BuildingType.COMMAND_CITADEL }
          items(buildable, key = { it.name }) { type ->
            val count = uiState.buildings.count { it.type == type }
            val canBuildMore = count < type.maxCount
            val cost = GameCatalog.buildingBuildCost(type, count, uiState.player.activeBiome)
            val affordable = uiState.player.resources.canAfford(cost)
            Card(
              modifier = Modifier.fillMaxWidth().testTag("build_catalog_${type.name}"),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
              border = BorderStroke(1.dp, if (canBuildMore) palette.primary.copy(alpha = 0.5f) else GameColors.SoftBorder),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CartoonBuildingIcon(type = type, size = 48.dp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${type.titleFa} ($count/${type.maxCount})",
                      style = MaterialTheme.typography.titleMedium,
                      color = GameColors.PureWhite,
                    )
                    Text(
                      text = type.descriptionFa,
                      style = MaterialTheme.typography.bodyMedium,
                      color = GameColors.OffWhite.copy(alpha = 0.8f),
                    )
                  }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ResourceCostRow(cost = cost, available = uiState.player.resources)
                Spacer(modifier = Modifier.height(10.dp))
                ChunkyGameButton(
                  text = if (!canBuildMore) "ظرفیت ساخت تکمیل است" else if (affordable) "ساخت ${type.titleFa}" else "منابع ناکافی",
                  enabled = canBuildMore && affordable,
                  icon = Icons.Rounded.AddCircle,
                  containerColor = GameColors.GoldYellow,
                  onClick = { onBuildBuilding(type) },
                  modifier = Modifier.fillMaxWidth().testTag("build_button_${type.name}"),
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
