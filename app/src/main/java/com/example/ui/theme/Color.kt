package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.BiomeType

object GameColors {
  val EmeraldGreen = Color(0xFF10B981)
  val EmeraldDark = Color(0xFF047857)
  val SkyBlue = Color(0xFF38BDF8)
  val Turquoise = Color(0xFF06B6D4)
  val OrangeBright = Color(0xFFF97316)
  val GoldYellow = Color(0xFFFACC15)
  val AmberGold = Color(0xFFF59E0B)
  val RoyalPurple = Color(0xFFA855F7)
  val CoralRed = Color(0xFFF43F5E)
  val DeepNavy = Color(0xFF0F172A)
  val MidnightNavy = Color(0xFF14213D)
  val SlateNavy = Color(0xFF1E293B)
  val PureWhite = Color(0xFFFFFFFF)
  val OffWhite = Color(0xFFF8FAFC)
  val SoftBorder = Color(0xFF334155)
}

data class BiomePalette(
  val primary: Color,
  val secondary: Color,
  val accent: Color,
  val skyTop: Color,
  val skyBottom: Color,
  val terrainLight: Color,
  val terrainDark: Color,
  val terrainEdge: Color,
  val cardSurface: Color,
)

fun biomePalette(biome: BiomeType): BiomePalette =
  when (biome) {
    BiomeType.EMERALD_MEADOW ->
      BiomePalette(
        primary = GameColors.EmeraldGreen,
        secondary = GameColors.SkyBlue,
        accent = GameColors.GoldYellow,
        skyTop = Color(0xFF0C4A6E),
        skyBottom = Color(0xFF0F2942),
        terrainLight = Color(0xFF34D399),
        terrainDark = Color(0xFF10B981),
        terrainEdge = Color(0xFF065F46),
        cardSurface = Color(0xFF132A3E),
      )
    BiomeType.FROSTY_PEAKS ->
      BiomePalette(
        primary = GameColors.Turquoise,
        secondary = GameColors.SkyBlue,
        accent = GameColors.PureWhite,
        skyTop = Color(0xFF0E3A5D),
        skyBottom = Color(0xFF0B1D33),
        terrainLight = Color(0xFFE0F2FE),
        terrainDark = Color(0xFFBAE6FD),
        terrainEdge = Color(0xFF0284C7),
        cardSurface = Color(0xFF152942),
      )
    BiomeType.GOLDEN_DUNES ->
      BiomePalette(
        primary = GameColors.OrangeBright,
        secondary = GameColors.AmberGold,
        accent = GameColors.Turquoise,
        skyTop = Color(0xFF451A03),
        skyBottom = Color(0xFF1E1B2E),
        terrainLight = Color(0xFFFCD34D),
        terrainDark = Color(0xFFF59E0B),
        terrainEdge = Color(0xFFB45309),
        cardSurface = Color(0xFF281D2B),
      )
    BiomeType.MYSTIC_CANOPY ->
      BiomePalette(
        primary = Color(0xFF22C55E),
        secondary = GameColors.RoyalPurple,
        accent = GameColors.Turquoise,
        skyTop = Color(0xFF064E3B),
        skyBottom = Color(0xFF111827),
        terrainLight = Color(0xFF4ADE80),
        terrainDark = Color(0xFF16A34A),
        terrainEdge = Color(0xFF14532D),
        cardSurface = Color(0xFF142B2D),
      )
    BiomeType.VOLCANIC_ISLE ->
      BiomePalette(
        primary = GameColors.CoralRed,
        secondary = GameColors.OrangeBright,
        accent = GameColors.GoldYellow,
        skyTop = Color(0xFF4C0519),
        skyBottom = Color(0xFF18181B),
        terrainLight = Color(0xFF57534E),
        terrainDark = Color(0xFF44403C),
        terrainEdge = Color(0xFFEA580C),
        cardSurface = Color(0xFF271722),
      )
    BiomeType.ARCANE_NEBULA ->
      BiomePalette(
        primary = GameColors.RoyalPurple,
        secondary = GameColors.Turquoise,
        accent = GameColors.GoldYellow,
        skyTop = Color(0xFF3B0764),
        skyBottom = Color(0xFF0F172A),
        terrainLight = Color(0xFFA78BFA),
        terrainDark = Color(0xFF7C3AED),
        terrainEdge = Color(0xFF4C1D95),
        cardSurface = Color(0xFF21163A),
      )
  }
