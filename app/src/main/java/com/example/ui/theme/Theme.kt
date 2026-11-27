package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.BiomeType

val LocalBiomePalette = staticCompositionLocalOf { biomePalette(BiomeType.EMERALD_MEADOW) }

private val GameShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
  )

@Composable
fun DejSetareganTheme(
  activeBiome: BiomeType = BiomeType.EMERALD_MEADOW,
  highContrast: Boolean = false,
  content: @Composable () -> Unit,
) {
  val palette = biomePalette(activeBiome)
  val colorScheme =
    darkColorScheme(
      primary = palette.primary,
      onPrimary = GameColors.DeepNavy,
      secondary = palette.secondary,
      onSecondary = GameColors.DeepNavy,
      tertiary = palette.accent,
      onTertiary = GameColors.DeepNavy,
      background = if (highContrast) GameColors.DeepNavy else palette.skyBottom,
      onBackground = GameColors.OffWhite,
      surface = if (highContrast) GameColors.SlateNavy else palette.cardSurface,
      onSurface = GameColors.OffWhite,
      surfaceVariant = GameColors.MidnightNavy,
      onSurfaceVariant = GameColors.OffWhite.copy(alpha = 0.85f),
      error = GameColors.CoralRed,
      onError = GameColors.PureWhite,
      outline = GameColors.SoftBorder,
    )

  CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
    LocalBiomePalette provides palette,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = GameShapes,
      content = content,
    )
  }
}
