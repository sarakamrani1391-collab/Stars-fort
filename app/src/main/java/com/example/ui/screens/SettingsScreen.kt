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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.ChunkyGameButton
import com.example.ui.theme.GameColors
import com.example.ui.viewmodel.GameUiState

@Composable
fun SettingsScreen(
  uiState: GameUiState,
  onToggleSound: (Boolean) -> Unit,
  onToggleAnimations: (Boolean) -> Unit,
  onToggleHighContrast: (Boolean) -> Unit,
  onReplayTutorial: () -> Unit,
  onResetGame: () -> Unit,
  onBack: () -> Unit,
) {
  var showResetConfirm by remember { mutableStateOf(false) }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(GameColors.MidnightNavy, GameColors.DeepNavy)))
        .safeDrawingPadding(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier =
        Modifier.widthIn(max = 680.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت", tint = GameColors.PureWhite)
        }
        Text("تنظیمات و آمار قلمرو", style = MaterialTheme.typography.headlineMedium, color = GameColors.GoldYellow)
      }

      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
        border = BorderStroke(1.dp, GameColors.SoftBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("تنظیمات تجربه بازی", style = MaterialTheme.typography.titleLarge, color = GameColors.SkyBlue)
          SettingToggleRow(
            title = "جلوه‌های صوتی و بازخورد",
            checked = uiState.preferences.soundEffectsEnabled,
            onCheckedChange = onToggleSound,
            tag = "toggle_sound",
          )
          SettingToggleRow(
            title = "انیمیشن‌ها و ذرات محیطی",
            checked = uiState.preferences.animationsEnabled,
            onCheckedChange = onToggleAnimations,
            tag = "toggle_animations",
          )
          SettingToggleRow(
            title = "حالت کنتراست بالا برای خوانایی بیشتر",
            checked = uiState.preferences.highContrastMode,
            onCheckedChange = onToggleHighContrast,
            tag = "toggle_contrast",
          )
        }
      }

      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
        border = BorderStroke(1.dp, GameColors.SoftBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("آمار ذخیره شده محلی (Room + DataStore)", style = MaterialTheme.typography.titleLarge, color = GameColors.EmeraldGreen)
          Text("نام قلمرو: ${uiState.player.kingdomName}", style = MaterialTheme.typography.bodyLarge, color = GameColors.PureWhite)
          Text("سطح فرمانده: ${uiState.player.level}", style = MaterialTheme.typography.bodyLarge, color = GameColors.PureWhite)
          Text("نبردهای پیروز: ${uiState.player.battlesWon}", style = MaterialTheme.typography.bodyLarge, color = GameColors.PureWhite)
          Text("مجموع ساختمان‌های ساخته شده: ${uiState.player.totalBuildingsBuilt}", style = MaterialTheme.typography.bodyLarge, color = GameColors.PureWhite)
          Text("مجموع نیروهای آموزش‌دیده: ${uiState.player.totalTroopsTrained}", style = MaterialTheme.typography.bodyLarge, color = GameColors.PureWhite)
          Text("منابع جمع‌آوری شده: ${uiState.player.totalResourcesCollected}", style = MaterialTheme.typography.bodyLarge, color = GameColors.PureWhite)
        }
      }

      ChunkyGameButton(
        text = "بازپخش آموزش تعاملی فرمانده سپهر",
        icon = Icons.Rounded.School,
        containerColor = GameColors.SkyBlue,
        onClick = {
          onReplayTutorial()
          onBack()
        },
        modifier = Modifier.fillMaxWidth().testTag("replay_tutorial_button"),
      )

      ChunkyGameButton(
        text = "شروع مجدد بازی از ابتدا",
        icon = Icons.Rounded.DeleteForever,
        containerColor = GameColors.CoralRed,
        contentColor = GameColors.PureWhite,
        onClick = { showResetConfirm = true },
        modifier = Modifier.fillMaxWidth().testTag("reset_game_button"),
      )
    }
  }

  if (showResetConfirm) {
    AlertDialog(
      onDismissRequest = { showResetConfirm = false },
      title = { Text("بازنشانی کامل اطلاعات؟") },
      text = { Text("تمام ساختمان‌ها، منابع، ستاره‌ها و پیشرفت شما روی دستگاه پاک شده و بازی از سطح ۱ شروع می‌شود.") },
      confirmButton = {
        TextButton(
          onClick = {
            showResetConfirm = false
            onResetGame()
            onBack()
          }
        ) {
          Text("بله، شروع مجدد", color = GameColors.CoralRed)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirm = false }) {
          Text("انصراف")
        }
      },
    )
  }
}

@Composable
private fun SettingToggleRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  tag: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = title, style = MaterialTheme.typography.bodyLarge, color = GameColors.PureWhite, modifier = Modifier.weight(1f))
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(tag),
    )
  }
}
