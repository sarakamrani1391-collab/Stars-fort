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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.model.BiomeType
import com.example.ui.components.ChunkyGameButton
import com.example.ui.theme.GameColors
import com.example.ui.theme.biomePalette
import com.example.ui.viewmodel.GameUiState

@Composable
fun RealmSelectScreen(
  uiState: GameUiState,
  onSelectBiome: (BiomeType, String) -> Unit,
  onBack: () -> Unit,
) {
  var kingdomNameInput by remember(uiState.player.kingdomName) {
    mutableStateOf(uiState.player.kingdomName)
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(GameColors.MidnightNavy, GameColors.DeepNavy)))
        .safeDrawingPadding(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize().padding(horizontal = 16.dp),
    ) {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("realm_back_button")) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "بازگشت",
            tint = GameColors.PureWhite,
          )
        }
        Text(
          text = "انتخاب و ساخت قلمرو",
          style = MaterialTheme.typography.headlineMedium,
          color = GameColors.GoldYellow,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = kingdomNameInput,
        onValueChange = { if (it.length <= 24) kingdomNameInput = it },
        label = { Text("نام اختصاصی پایگاه شما") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("kingdom_name_input"),
        shape = RoundedCornerShape(16.dp),
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GameColors.Turquoise,
            unfocusedBorderColor = GameColors.SoftBorder,
            focusedTextColor = GameColors.PureWhite,
            unfocusedTextColor = GameColors.PureWhite,
            focusedLabelColor = GameColors.Turquoise,
          ),
      )

      Spacer(modifier = Modifier.height(14.dp))

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(BiomeType.entries) { biome ->
          val unlocked = uiState.player.level >= biome.unlockLevel
          val isActive = uiState.player.activeBiome == biome
          val bPalette = biomePalette(biome)

          Card(
            modifier = Modifier.fillMaxWidth().testTag("biome_card_${biome.name}"),
            shape = RoundedCornerShape(22.dp),
            colors =
              CardDefaults.cardColors(
                containerColor = if (isActive) bPalette.cardSurface else GameColors.MidnightNavy
              ),
            border =
              BorderStroke(
                width = if (isActive) 2.5.dp else 1.dp,
                color = if (isActive) bPalette.primary else GameColors.SoftBorder,
              ),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = bPalette.primary.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(44.dp),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = if (unlocked) Icons.Rounded.Public else Icons.Rounded.Lock,
                        contentDescription = biome.titleFa,
                        tint = if (unlocked) bPalette.primary else GameColors.SoftBorder,
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = biome.titleFa,
                      style = MaterialTheme.typography.titleLarge,
                      color = GameColors.PureWhite,
                    )
                    Text(
                      text = biome.subtitleFa,
                      style = MaterialTheme.typography.bodyMedium,
                      color = bPalette.secondary,
                    )
                  }
                }

                if (isActive) {
                  Surface(
                    color = bPalette.primary,
                    shape = RoundedCornerShape(50),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = GameColors.DeepNavy, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("فعال", style = MaterialTheme.typography.labelSmall, color = GameColors.DeepNavy)
                    }
                  }
                } else if (!unlocked) {
                  Surface(color = GameColors.SlateNavy, shape = RoundedCornerShape(50)) {
                    Text(
                      text = "نیاز به سطح ${biome.unlockLevel}",
                      style = MaterialTheme.typography.labelSmall,
                      color = GameColors.GoldYellow,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = biome.challengeFa,
                style = MaterialTheme.typography.bodyMedium,
                color = GameColors.OffWhite.copy(alpha = 0.85f),
              )
              Spacer(modifier = Modifier.height(12.dp))

              ChunkyGameButton(
                text = if (isActive) "ذخیره نام در این قلمرو" else if (unlocked) "انتخاب این قلمرو" else "قفل (سطح ${biome.unlockLevel})",
                enabled = unlocked,
                containerColor = if (isActive) bPalette.secondary else bPalette.primary,
                onClick = { onSelectBiome(biome, kingdomNameInput) },
                modifier = Modifier.fillMaxWidth().testTag("select_biome_${biome.name}"),
              )
            }
          }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
      }
    }
  }
}
