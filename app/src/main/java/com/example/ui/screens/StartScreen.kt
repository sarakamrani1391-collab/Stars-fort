package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.ChunkyGameButton
import com.example.ui.theme.GameColors
import com.example.ui.theme.LocalBiomePalette
import com.example.ui.viewmodel.GameUiState

@Composable
fun StartScreen(
  uiState: GameUiState,
  onContinueGame: () -> Unit,
  onSelectRealm: () -> Unit,
  onOpenSettings: () -> Unit,
) {
  val palette = LocalBiomePalette.current
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(listOf(palette.skyTop, GameColors.MidnightNavy, GameColors.DeepNavy))
        )
        .safeDrawingPadding(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier.widthIn(max = 560.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
          color = GameColors.EmeraldGreen.copy(alpha = 0.2f),
          shape = RoundedCornerShape(50),
          border = BorderStroke(1.dp, GameColors.EmeraldGreen),
        ) {
          Text(
            text = "نسخه آفلاین و کامل • بدون نیاز به اینترنت",
            style = MaterialTheme.typography.labelSmall,
            color = GameColors.EmeraldGreen,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.displayLarge,
          color = GameColors.GoldYellow,
          textAlign = TextAlign.Center,
        )
        Text(
          text = "ساخت قلمرو، تربیت قهرمانان و نبردهای تاکتیکی فانتزی",
          style = MaterialTheme.typography.bodyLarge,
          color = GameColors.OffWhite.copy(alpha = 0.88f),
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
          shape = RoundedCornerShape(26.dp),
          border = BorderStroke(2.5.dp, GameColors.GoldYellow.copy(alpha = 0.7f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box {
            Image(
              painter = painterResource(id = R.drawable.img_hero_citadel),
              contentDescription = stringResource(R.string.cd_hero_banner),
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(26.dp)),
            )
            Surface(
              color = GameColors.DeepNavy.copy(alpha = 0.82f),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = GameColors.SkyBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${uiState.player.kingdomName} • سطح ${uiState.player.level}",
                  style = MaterialTheme.typography.labelLarge,
                  color = GameColors.PureWhite,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy.copy(alpha = 0.85f)),
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(1.dp, GameColors.SoftBorder),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            StartStatChip(label = "قلمرو فعال", value = uiState.player.activeBiome.titleFa, color = palette.primary)
            StartStatChip(label = "ستاره‌ها", value = "${uiState.player.totalStars} ★", color = GameColors.GoldYellow)
            StartStatChip(label = "قدرت ارتش", value = "${uiState.armyPower}", color = GameColors.Turquoise)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChunkyGameButton(
          text = "ورود به قلمرو و ادامه بازی",
          onClick = onContinueGame,
          icon = Icons.Rounded.PlayArrow,
          containerColor = GameColors.EmeraldGreen,
          contentColor = GameColors.DeepNavy,
          modifier = Modifier.fillMaxWidth().height(56.dp).testTag("start_continue_button"),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          ChunkyGameButton(
            text = "انتخاب قلمرو",
            onClick = onSelectRealm,
            icon = Icons.Rounded.Explore,
            containerColor = GameColors.SkyBlue,
            contentColor = GameColors.DeepNavy,
            modifier = Modifier.weight(1f).testTag("start_realms_button"),
          )
          OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.weight(1f).height(50.dp).testTag("start_settings_button"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, GameColors.SoftBorder),
          ) {
            Icon(Icons.Rounded.Settings, contentDescription = null, tint = GameColors.PureWhite)
            Spacer(modifier = Modifier.width(6.dp))
            Text("تنظیمات", color = GameColors.PureWhite, style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }
  }
}

@Composable
private fun StartStatChip(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = GameColors.OffWhite.copy(alpha = 0.7f))
    Spacer(modifier = Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Rounded.Star, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(text = value, style = MaterialTheme.typography.titleMedium, color = color)
    }
  }
}
