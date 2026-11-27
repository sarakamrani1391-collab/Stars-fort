package com.example.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.model.BiomeType
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.model.PlacedBuilding
import com.example.ui.theme.GameColors
import com.example.ui.theme.biomePalette
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

internal data class IsoMetrics(
  val originX: Float,
  val originY: Float,
  val tileW: Float,
  val tileH: Float,
) {
  fun gridToScreen(gx: Float, gy: Float): Offset {
    val sx = originX + (gx - gy) * (tileW / 2f)
    val sy = originY + (gx + gy) * (tileH / 2f)
    return Offset(sx, sy)
  }

  fun screenToGrid(sx: Float, sy: Float): Pair<Int, Int>? {
    val dx = sx - originX
    val dy = sy - originY
    val halfW = tileW / 2f
    val halfH = tileH / 2f
    val gx = floor(((dx / halfW + dy / halfH) / 2f)).toInt()
    val gy = floor(((dy / halfH - dx / halfW) / 2f)).toInt()
    return if (gx in 0 until GameCatalog.GRID_SIZE && gy in 0 until GameCatalog.GRID_SIZE) {
      gx to gy
    } else {
      null
    }
  }
}

internal fun computeIsoMetrics(canvasWidth: Float, canvasHeight: Float): IsoMetrics {
  val n = GameCatalog.GRID_SIZE.toFloat()
  val tileW = (canvasWidth * 0.90f) / n
  val tileH = tileW * 0.52f
  val totalIsoHeight = n * tileH
  val originX = canvasWidth / 2f
  val originY = ((canvasHeight - totalIsoHeight) / 2f).coerceAtLeast(canvasHeight * 0.14f)
  return IsoMetrics(originX = originX, originY = originY, tileW = tileW, tileH = tileH)
}

@Composable
fun IsometricBaseCanvas(
  biome: BiomeType,
  buildings: List<PlacedBuilding>,
  selectedBuildingId: Long?,
  movingBuildingId: Long?,
  selectedEmptyCell: Pair<Int, Int>? = null,
  isPlacingMode: Boolean = false,
  animationsEnabled: Boolean,
  onGridTap: (Int, Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = biomePalette(biome)
  val infiniteTransition = rememberInfiniteTransition(label = "baseAnim")
  val animPhase by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 4500, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "phase",
    )
  val phase = if (animationsEnabled) animPhase else 0.25f

  Canvas(
    modifier =
      modifier.fillMaxSize().pointerInput(buildings, movingBuildingId) {
        detectTapGestures { offset ->
          val metrics = computeIsoMetrics(size.width.toFloat(), size.height.toFloat())
          val cell = metrics.screenToGrid(offset.x, offset.y)
          if (cell != null) {
            onGridTap(cell.first, cell.second)
          }
        }
      }
  ) {
    val w = size.width
    val h = size.height
    drawRect(brush = Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom)))

    val metrics = computeIsoMetrics(w, h)
    val n = GameCatalog.GRID_SIZE

    // Floating island base shadow & cliff rim
    val topPt = metrics.gridToScreen(0f, 0f)
    val rightPt = metrics.gridToScreen(n.toFloat(), 0f)
    val bottomPt = metrics.gridToScreen(n.toFloat(), n.toFloat())
    val leftPt = metrics.gridToScreen(0f, n.toFloat())
    val cliffDepth = metrics.tileH * 0.95f

    val cliffPath =
      Path().apply {
        moveTo(leftPt.x, leftPt.y)
        lineTo(bottomPt.x, bottomPt.y)
        lineTo(rightPt.x, rightPt.y)
        lineTo(rightPt.x, rightPt.y + cliffDepth)
        lineTo(bottomPt.x, bottomPt.y + cliffDepth)
        lineTo(leftPt.x, leftPt.y + cliffDepth)
        close()
      }
    drawPath(cliffPath, palette.terrainEdge)

    val occupiedMap = buildings.associateBy { it.gridX to it.gridY }

    // Draw isometric checkerboard tiles
    for (gy in 0 until n) {
      for (gx in 0 until n) {
        val isLight = (gx + gy) % 2 == 0
        val tileColor = if (isLight) palette.terrainLight else palette.terrainDark
        drawIsoDiamond(metrics, gx.toFloat(), gy.toFloat(), tileColor)

        // Move or place mode indicators
        if (movingBuildingId != null || isPlacingMode) {
          val occupant = occupiedMap[gx to gy]
          if (occupant == null) {
            drawIsoDiamond(
              metrics,
              gx.toFloat(),
              gy.toFloat(),
              GameColors.Turquoise.copy(alpha = 0.35f),
              strokeColor = GameColors.PureWhite.copy(alpha = 0.7f),
            )
          }
        }
        if (selectedEmptyCell != null && selectedEmptyCell.first == gx && selectedEmptyCell.second == gy) {
          drawIsoDiamond(
            metrics,
            gx.toFloat(),
            gy.toFloat(),
            GameColors.SkyBlue.copy(alpha = 0.42f),
            strokeColor = GameColors.GoldYellow,
          )
        }
      }
    }

    // Painter's algorithm sort: back-to-front by (gridX + gridY)
    val sortedBuildings = buildings.sortedBy { it.gridX + it.gridY }
    for (b in sortedBuildings) {
      val isSelected = b.id == selectedBuildingId || b.id == movingBuildingId
      if (isSelected) {
        drawIsoDiamond(
          metrics,
          b.gridX.toFloat(),
          b.gridY.toFloat(),
          GameColors.GoldYellow.copy(alpha = 0.38f),
          strokeColor = GameColors.GoldYellow,
        )
      }
      drawIsometricBuilding(
        metrics = metrics,
        gx = b.gridX.toFloat(),
        gy = b.gridY.toFloat(),
        type = b.type,
        level = b.level,
        hpRatio = 1f,
        phase = phase,
      )
      val storedTotal = b.storedGold + b.storedWood + b.storedStone + b.storedCrystal
      if (storedTotal > 0) {
        val center = metrics.gridToScreen(b.gridX + 0.5f, b.gridY + 0.5f)
        val bob = sin(phase * 2f * PI.toFloat()) * 4f
        val bubbleCenter = Offset(center.x, center.y - metrics.tileH * 1.45f + bob)
        drawCircle(color = GameColors.MidnightNavy, radius = metrics.tileW * 0.16f, center = bubbleCenter)
        drawCircle(
          color =
            when (b.type) {
              BuildingType.GOLD_MINE -> GameColors.GoldYellow
              BuildingType.RESOURCE_MILL -> GameColors.EmeraldGreen
              else -> GameColors.Turquoise
            },
          radius = metrics.tileW * 0.12f,
          center = bubbleCenter,
        )
      }
    }

    // Biome atmospheric particles
    drawBiomeWeatherParticles(biome, w, h, phase)
  }
}

internal fun DrawScope.drawIsoDiamond(
  metrics: IsoMetrics,
  gx: Float,
  gy: Float,
  fillColor: Color,
  strokeColor: Color? = Color.Black.copy(alpha = 0.08f),
) {
  val top = metrics.gridToScreen(gx, gy)
  val right = metrics.gridToScreen(gx + 1f, gy)
  val bottom = metrics.gridToScreen(gx + 1f, gy + 1f)
  val left = metrics.gridToScreen(gx, gy + 1f)
  val path =
    Path().apply {
      moveTo(top.x, top.y)
      lineTo(right.x, right.y)
      lineTo(bottom.x, bottom.y)
      lineTo(left.x, left.y)
      close()
    }
  drawPath(path, fillColor)
  if (strokeColor != null) {
    drawPath(path, strokeColor, style = Stroke(width = 1.5f))
  }
}

internal fun DrawScope.drawIsometricBuilding(
  metrics: IsoMetrics,
  gx: Float,
  gy: Float,
  type: BuildingType,
  level: Int,
  hpRatio: Float,
  phase: Float,
) {
  val center = metrics.gridToScreen(gx + 0.5f, gy + 0.5f)
  val baseW = metrics.tileW * 0.62f
  val baseH = metrics.tileH * 0.62f
  val height =
    when (type) {
      BuildingType.COMMAND_CITADEL -> metrics.tileH * (1.25f + level * 0.08f)
      BuildingType.DEFENSE_TOWER -> metrics.tileH * (1.15f + level * 0.07f)
      BuildingType.PROTECTIVE_WALL -> metrics.tileH * 0.55f
      BuildingType.TECH_LABORATORY -> metrics.tileH * 0.95f
      else -> metrics.tileH * 0.80f
    }

  val wallColor =
    when (type) {
      BuildingType.COMMAND_CITADEL -> Color(0xFFFDE68A)
      BuildingType.CITIZEN_COTTAGE -> Color(0xFFBAE6FD)
      BuildingType.HERO_BARRACKS -> Color(0xFFFECDD3)
      BuildingType.GOLD_MINE -> Color(0xFFFCD34D)
      BuildingType.RESOURCE_MILL -> Color(0xFFA7F3D0)
      BuildingType.CRYSTAL_EXTRACTOR -> Color(0xFFA5F3FC)
      BuildingType.RESOURCE_VAULT -> Color(0xFFCBD5E1)
      BuildingType.TECH_LABORATORY -> Color(0xFFE9D5FF)
      BuildingType.DEFENSE_TOWER -> Color(0xFFFED7AA)
      BuildingType.PROTECTIVE_WALL -> Color(0xFF94A3B8)
    }

  val roofColor =
    when (type) {
      BuildingType.COMMAND_CITADEL -> GameColors.OrangeBright
      BuildingType.CITIZEN_COTTAGE -> GameColors.SkyBlue
      BuildingType.HERO_BARRACKS -> GameColors.CoralRed
      BuildingType.GOLD_MINE -> GameColors.AmberGold
      BuildingType.RESOURCE_MILL -> GameColors.EmeraldGreen
      BuildingType.CRYSTAL_EXTRACTOR -> GameColors.Turquoise
      BuildingType.RESOURCE_VAULT -> Color(0xFF64748B)
      BuildingType.TECH_LABORATORY -> GameColors.RoyalPurple
      BuildingType.DEFENSE_TOWER -> GameColors.CoralRed
      BuildingType.PROTECTIVE_WALL -> Color(0xFF64748B)
    }

  // Ground shadow
  drawIsoDiamond(metrics, gx + 0.08f, gy + 0.08f, Color.Black.copy(alpha = 0.2f), strokeColor = null)

  val pTop = Offset(center.x, center.y - baseH / 2f)
  val pRight = Offset(center.x + baseW / 2f, center.y)
  val pBottom = Offset(center.x, center.y + baseH / 2f)
  val pLeft = Offset(center.x - baseW / 2f, center.y)

  val uTop = Offset(pTop.x, pTop.y - height)
  val uRight = Offset(pRight.x, pRight.y - height)
  val uBottom = Offset(pBottom.x, pBottom.y - height)
  val uLeft = Offset(pLeft.x, pLeft.y - height)

  // Left extruded wall
  val leftFace =
    Path().apply {
      moveTo(pLeft.x, pLeft.y)
      lineTo(pBottom.x, pBottom.y)
      lineTo(uBottom.x, uBottom.y)
      lineTo(uLeft.x, uLeft.y)
      close()
    }
  drawPath(leftFace, wallColor.copy(alpha = 0.85f))

  // Right extruded wall (slightly darker for 3D depth)
  val rightFace =
    Path().apply {
      moveTo(pBottom.x, pBottom.y)
      lineTo(pRight.x, pRight.y)
      lineTo(uRight.x, uRight.y)
      lineTo(uBottom.x, uBottom.y)
      close()
    }
  drawPath(rightFace, wallColor.copy(alpha = 0.65f))

  // Top roof / cap
  if (type == BuildingType.PROTECTIVE_WALL || type == BuildingType.RESOURCE_VAULT) {
    val roofFace =
      Path().apply {
        moveTo(uTop.x, uTop.y)
        lineTo(uRight.x, uRight.y)
        lineTo(uBottom.x, uBottom.y)
        lineTo(uLeft.x, uLeft.y)
        close()
      }
    drawPath(roofFace, roofColor)
  } else {
    val apex = Offset(center.x, uTop.y - metrics.tileH * 0.42f)
    val roofLeft =
      Path().apply {
        moveTo(apex.x, apex.y)
        lineTo(uLeft.x, uLeft.y)
        lineTo(uBottom.x, uBottom.y)
        close()
      }
    val roofRight =
      Path().apply {
        moveTo(apex.x, apex.y)
        lineTo(uBottom.x, uBottom.y)
        lineTo(uRight.x, uRight.y)
        close()
      }
    val roofBack =
      Path().apply {
        moveTo(apex.x, apex.y)
        lineTo(uLeft.x, uLeft.y)
        lineTo(uTop.x, uTop.y)
        lineTo(uRight.x, uRight.y)
        close()
      }
    drawPath(roofBack, roofColor.copy(alpha = 0.78f))
    drawPath(roofLeft, roofColor)
    drawPath(roofRight, roofColor.copy(alpha = 0.88f))

    // Special glowing jewel on Citadel, Tower, Crystal Extractor, or Lab
    if (
      type == BuildingType.COMMAND_CITADEL ||
        type == BuildingType.DEFENSE_TOWER ||
        type == BuildingType.CRYSTAL_EXTRACTOR ||
        type == BuildingType.TECH_LABORATORY
    ) {
      val pulse = 1f + 0.15f * sin(phase * 2f * PI.toFloat())
      val orbColor =
        when (type) {
          BuildingType.COMMAND_CITADEL -> GameColors.GoldYellow
          BuildingType.DEFENSE_TOWER -> GameColors.OrangeBright
          BuildingType.CRYSTAL_EXTRACTOR -> GameColors.Turquoise
          else -> GameColors.RoyalPurple
        }
      drawCircle(color = orbColor, radius = metrics.tileW * 0.08f * pulse, center = apex)
    }
  }

  // Health bar during battle
  if (hpRatio < 0.999f && hpRatio > 0f) {
    val barW = metrics.tileW * 0.56f
    val barH = 6f
    val barTop = uTop.y - metrics.tileH * 0.55f
    drawRect(
      color = GameColors.DeepNavy.copy(alpha = 0.85f),
      topLeft = Offset(center.x - barW / 2f, barTop),
      size = androidx.compose.ui.geometry.Size(barW, barH),
    )
    drawRect(
      color = if (hpRatio > 0.4f) GameColors.EmeraldGreen else GameColors.CoralRed,
      topLeft = Offset(center.x - barW / 2f, barTop),
      size = androidx.compose.ui.geometry.Size(barW * hpRatio, barH),
    )
  }
}

internal fun DrawScope.drawBiomeWeatherParticles(
  biome: BiomeType,
  width: Float,
  height: Float,
  phase: Float,
) {
  val particleColor =
    when (biome) {
      BiomeType.EMERALD_MEADOW -> Color.White.copy(alpha = 0.35f)
      BiomeType.FROSTY_PEAKS -> Color.White.copy(alpha = 0.75f)
      BiomeType.GOLDEN_DUNES -> GameColors.GoldYellow.copy(alpha = 0.45f)
      BiomeType.MYSTIC_CANOPY -> Color(0xFFBEF264).copy(alpha = 0.65f)
      BiomeType.VOLCANIC_ISLE -> GameColors.OrangeBright.copy(alpha = 0.7f)
      BiomeType.ARCANE_NEBULA -> GameColors.Turquoise.copy(alpha = 0.65f)
    }
  val count = 14
  for (i in 0 until count) {
    val seedX = ((i * 73 + 19) % 100) / 100f
    val seedY = ((i * 41 + 7) % 100) / 100f
    val x =
      if (biome == BiomeType.GOLDEN_DUNES) {
        ((seedX + phase) % 1f) * width
      } else {
        seedX * width + sin((phase + i * 0.1f) * 2f * PI.toFloat()) * 14f
      }
    val y =
      when (biome) {
        BiomeType.VOLCANIC_ISLE -> ((1f - (seedY + phase) % 1f) * height)
        else -> ((seedY + phase) % 1f) * height
      }
    val r = 2.5f + (i % 3) * 1.6f
    drawCircle(color = particleColor, radius = r, center = Offset(x, y))
  }
}
