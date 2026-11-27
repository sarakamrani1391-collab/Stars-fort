package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.model.BattleSimulationState
import com.example.model.BiomeType
import com.example.model.GameCatalog
import com.example.model.TroopType
import com.example.ui.theme.GameColors
import com.example.ui.theme.biomePalette
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun BattleArenaCanvas(
  battleState: BattleSimulationState,
  onDeployTap: (Float, Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val biome = battleState.stage?.biome ?: BiomeType.EMERALD_MEADOW
  val palette = biomePalette(biome)
  val phase = (battleState.elapsedSeconds * 0.6f) % 1f

  Canvas(
    modifier =
      modifier.fillMaxSize().pointerInput(battleState.isDefenseTest, battleState.selectedDeployTroop) {
        detectTapGestures { offset ->
          if (!battleState.isDefenseTest && !battleState.isFinished) {
            val metrics = computeIsoMetrics(size.width.toFloat(), size.height.toFloat())
            val cell = metrics.screenToGrid(offset.x, offset.y)
            if (cell != null) {
              onDeployTap(cell.first + 0.5f, cell.second.coerceAtLeast(5) + 0.5f)
            } else {
              onDeployTap(3.5f, 6.2f)
            }
          }
        }
      }
  ) {
    val w = size.width
    val h = size.height
    drawRect(brush = Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom)))
    val metrics = computeIsoMetrics(w, h)
    val n = GameCatalog.GRID_SIZE

    // Arena ground tiles
    for (gy in 0 until n) {
      for (gx in 0 until n) {
        val isDeployZone = !battleState.isDefenseTest && gy >= 5
        val baseColor = if ((gx + gy) % 2 == 0) palette.terrainLight else palette.terrainDark
        val finalColor =
          if (isDeployZone) {
            Color(
              red = (baseColor.red * 0.8f + 0.1f).coerceIn(0f, 1f),
              green = (baseColor.green * 0.85f + 0.1f).coerceIn(0f, 1f),
              blue = (baseColor.blue * 0.8f + 0.2f).coerceIn(0f, 1f),
            )
          } else {
            baseColor
          }
        drawIsoDiamond(
          metrics = metrics,
          gx = gx.toFloat(),
          gy = gy.toFloat(),
          fillColor = finalColor,
          strokeColor =
            if (isDeployZone) GameColors.Turquoise.copy(alpha = 0.45f)
            else Color.Black.copy(alpha = 0.08f),
        )
      }
    }

    // Draw surviving buildings
    val liveBuildings =
      battleState.buildings.filter { !it.isDestroyed }.sortedBy { it.gridX + it.gridY }
    for (b in liveBuildings) {
      drawIsometricBuilding(
        metrics = metrics,
        gx = b.gridX,
        gy = b.gridY,
        type = b.type,
        level = b.level,
        hpRatio = b.hp.toFloat() / b.maxHp.coerceAtLeast(1),
        phase = phase,
      )
    }

    // Draw live cartoon units
    val liveUnits = battleState.units.filter { it.isAlive }.sortedBy { it.x + it.y }
    for (u in liveUnits) {
      val pos = metrics.gridToScreen(u.x, u.y)
      val hop = kotlin.math.abs(sin((battleState.elapsedSeconds * 5f + u.id) * PI.toFloat())) * 5f
      val unitCenter = Offset(pos.x, pos.y - metrics.tileH * 0.25f - hop)
      val unitColor =
        if (u.isEnemy) {
          GameColors.CoralRed
        } else {
          when (u.type) {
            TroopType.FOOT_SOLDIER -> GameColors.SkyBlue
            TroopType.ARCHER -> GameColors.EmeraldGreen
            TroopType.ENGINEER -> GameColors.OrangeBright
            TroopType.HEAVY_GUARDIAN -> Color(0xFFCBD5E1)
            TroopType.SCOUT_RIDER -> GameColors.GoldYellow
            TroopType.STAR_MAGE -> GameColors.RoyalPurple
          }
        }
      val r =
        if (u.type == TroopType.HEAVY_GUARDIAN) metrics.tileW * 0.16f else metrics.tileW * 0.115f

      // Shadow
      drawCircle(color = Color.Black.copy(alpha = 0.28f), radius = r * 0.9f, center = pos)
      // Body
      drawCircle(color = unitColor, radius = r, center = unitCenter)
      drawCircle(color = GameColors.PureWhite, radius = r * 0.35f, center = Offset(unitCenter.x, unitCenter.y - r * 0.2f))

      // Mini HP bar
      val hpRatio = (u.hp.toFloat() / u.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
      val barW = r * 2.2f
      drawRect(
        color = GameColors.DeepNavy,
        topLeft = Offset(unitCenter.x - barW / 2f, unitCenter.y - r - 8f),
        size = Size(barW, 4f),
      )
      drawRect(
        color = if (u.isEnemy) GameColors.CoralRed else GameColors.EmeraldGreen,
        topLeft = Offset(unitCenter.x - barW / 2f, unitCenter.y - r - 8f),
        size = Size(barW * hpRatio, 4f),
      )
    }

    // Projectiles
    for (p in battleState.projectiles) {
      val from = metrics.gridToScreen(p.fromX + 0.5f, p.fromY + 0.5f)
      val to = metrics.gridToScreen(p.toX + 0.5f, p.toY + 0.5f)
      val cur =
        Offset(
          x = from.x + (to.x - from.x) * p.progress,
          y = from.y + (to.y - from.y) * p.progress - metrics.tileH * 0.45f,
        )
      drawCircle(color = Color(p.colorHex), radius = 6f, center = cur)
      drawCircle(color = Color.White, radius = 3f, center = cur)
    }

    // Cartoon star-puff bursts (non-violent fantasy effect)
    for (fx in battleState.effects) {
      val pos = metrics.gridToScreen(fx.x + 0.5f, fx.y + 0.5f)
      val alpha = (1f - fx.age / 0.7f).coerceIn(0f, 1f)
      val radius = metrics.tileW * (0.18f + fx.age * 0.4f)
      val color = if (fx.isStarBurst) GameColors.GoldYellow else GameColors.SkyBlue
      drawCircle(color = color.copy(alpha = alpha * 0.65f), radius = radius, center = pos)
      drawCircle(color = Color.White.copy(alpha = alpha * 0.8f), radius = radius * 0.45f, center = pos)
    }

    drawBiomeWeatherParticles(biome, w, h, phase)
  }
}
