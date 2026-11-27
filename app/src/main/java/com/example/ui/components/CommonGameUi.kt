package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Hardware
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BuildingType
import com.example.model.PlayerProfile
import com.example.model.ResourceBundle
import com.example.model.ResourceType
import com.example.model.TroopType
import com.example.model.TutorialStep
import com.example.ui.theme.GameColors
import com.example.ui.theme.LocalBiomePalette

@Composable
fun ResourceTopHud(
  player: PlayerProfile,
  storageCap: ResourceBundle,
  modifier: Modifier = Modifier,
) {
  val palette = LocalBiomePalette.current
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = GameColors.MidnightNavy.copy(alpha = 0.92f),
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.45f)),
    tonalElevation = 6.dp,
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier =
              Modifier.size(34.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(listOf(GameColors.GoldYellow, GameColors.OrangeBright))
                ),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "${player.level}",
              style = MaterialTheme.typography.titleMedium,
              color = GameColors.DeepNavy,
              fontWeight = FontWeight.ExtraBold,
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = player.kingdomName,
              style = MaterialTheme.typography.titleMedium,
              color = GameColors.PureWhite,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${player.activeBiome.titleFa} • ${player.totalStars} ★",
              style = MaterialTheme.typography.labelSmall,
              color = palette.secondary,
            )
          }
        }

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(105.dp)) {
          Text(
            text = "تجربه ${player.xp}/${player.xpForNextLevel}",
            style = MaterialTheme.typography.labelSmall,
            color = GameColors.GoldYellow,
          )
          Spacer(modifier = Modifier.height(3.dp))
          val animProgress by animateFloatAsState(player.xpProgress, spring(), label = "xp")
          LinearProgressIndicator(
            progress = { animProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = GameColors.GoldYellow,
            trackColor = GameColors.SlateNavy,
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        ResourcePill(
          icon = Icons.Rounded.MonetizationOn,
          color = GameColors.GoldYellow,
          amount = player.resources.gold,
          cap = storageCap.gold,
          label = ResourceType.GOLD.titleFa,
          modifier = Modifier.weight(1f),
        )
        ResourcePill(
          icon = Icons.Rounded.Forest,
          color = GameColors.EmeraldGreen,
          amount = player.resources.wood,
          cap = storageCap.wood,
          label = ResourceType.WOOD.titleFa,
          modifier = Modifier.weight(1f),
        )
        ResourcePill(
          icon = Icons.Rounded.Hardware,
          color = Color(0xFFCBD5E1),
          amount = player.resources.stone,
          cap = storageCap.stone,
          label = ResourceType.STONE.titleFa,
          modifier = Modifier.weight(1f),
        )
        ResourcePill(
          icon = Icons.Rounded.AutoAwesome,
          color = GameColors.Turquoise,
          amount = player.resources.crystal,
          cap = storageCap.crystal,
          label = ResourceType.CRYSTAL.titleFa,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
fun ResourcePill(
  icon: ImageVector,
  color: Color,
  amount: Int,
  cap: Int,
  label: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = GameColors.DeepNavy.copy(alpha = 0.85f),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = color,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "$amount",
        style = MaterialTheme.typography.labelLarge,
        color = if (amount >= cap) GameColors.GoldYellow else GameColors.PureWhite,
        maxLines = 1,
      )
    }
  }
}

@Composable
fun ResourceCostRow(cost: ResourceBundle, available: ResourceBundle? = null) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
    if (cost.gold > 0) {
      CostChip(
        value = cost.gold,
        color = GameColors.GoldYellow,
        icon = Icons.Rounded.MonetizationOn,
        enough = available == null || available.gold >= cost.gold,
        desc = "طلا",
      )
    }
    if (cost.wood > 0) {
      CostChip(
        value = cost.wood,
        color = GameColors.EmeraldGreen,
        icon = Icons.Rounded.Forest,
        enough = available == null || available.wood >= cost.wood,
        desc = "چوب",
      )
    }
    if (cost.stone > 0) {
      CostChip(
        value = cost.stone,
        color = Color(0xFFCBD5E1),
        icon = Icons.Rounded.Hardware,
        enough = available == null || available.stone >= cost.stone,
        desc = "سنگ",
      )
    }
    if (cost.crystal > 0) {
      CostChip(
        value = cost.crystal,
        color = GameColors.Turquoise,
        icon = Icons.Rounded.Diamond,
        enough = available == null || available.crystal >= cost.crystal,
        desc = "کریستال",
      )
    }
  }
}

@Composable
private fun CostChip(
  value: Int,
  color: Color,
  icon: ImageVector,
  enough: Boolean,
  desc: String,
) {
  Surface(
    color = if (enough) GameColors.DeepNavy else GameColors.CoralRed.copy(alpha = 0.25f),
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, if (enough) color.copy(alpha = 0.4f) else GameColors.CoralRed),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(icon, contentDescription = desc, tint = color, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = "$value",
        style = MaterialTheme.typography.labelSmall,
        color = if (enough) GameColors.PureWhite else GameColors.CoralRed,
      )
    }
  }
}

@Composable
fun ChunkyGameButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = GameColors.EmeraldGreen,
  contentColor: Color = GameColors.DeepNavy,
  enabled: Boolean = true,
  icon: ImageVector? = null,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.height(50.dp),
    shape = RoundedCornerShape(16.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = GameColors.SoftBorder,
        disabledContentColor = GameColors.OffWhite.copy(alpha = 0.5f),
      ),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
  ) {
    if (icon != null) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
  }
}

@Composable
fun TutorialCommanderBanner(
  step: TutorialStep,
  onSkip: () -> Unit,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(visible = step != TutorialStep.COMPLETED, modifier = modifier) {
    Card(
      modifier = Modifier.fillMaxWidth().testTag("tutorial_banner"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = GameColors.MidnightNavy),
      border = BorderStroke(2.dp, GameColors.GoldYellow),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_commander_guide),
          contentDescription = stringResource(R.string.cd_commander_avatar),
          contentScale = ContentScale.Crop,
          modifier =
            Modifier.size(56.dp)
              .clip(CircleShape)
              .border(2.dp, GameColors.GoldYellow, CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${step.titleFa} (${step.stepIndex + 1}/4)",
            style = MaterialTheme.typography.titleMedium,
            color = GameColors.GoldYellow,
          )
          Text(
            text = step.messageFa,
            style = MaterialTheme.typography.bodyMedium,
            color = GameColors.PureWhite,
          )
          if (step.actionHintFa.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              color = GameColors.Turquoise.copy(alpha = 0.2f),
              shape = RoundedCornerShape(10.dp),
              modifier =
                if (onActionClick != null) {
                  Modifier.clickable(onClick = onActionClick)
                } else {
                  Modifier
                },
            ) {
              Text(
                text = "راهنما: ${step.actionHintFa}",
                style = MaterialTheme.typography.labelSmall,
                color = GameColors.SkyBlue,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
        }
        IconButton(
          onClick = onSkip,
          modifier = Modifier.testTag("skip_tutorial_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "رد کردن آموزش",
            tint = GameColors.OffWhite.copy(alpha = 0.75f),
          )
        }
      }
    }
  }
}

@Composable
fun StarRatingRow(stars: Int, maxStars: Int = 3, starSize: Dp = 22.dp) {
  Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
    for (i in 1..maxStars) {
      Icon(
        imageVector = if (i <= stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
        contentDescription = "ستاره $i",
        tint = if (i <= stars) GameColors.GoldYellow else GameColors.SoftBorder,
        modifier = Modifier.size(starSize),
      )
    }
  }
}

@Composable
fun CartoonTroopBadge(type: TroopType, size: Dp = 52.dp) {
  val bg =
    when (type) {
      TroopType.FOOT_SOLDIER -> GameColors.SkyBlue
      TroopType.ARCHER -> GameColors.EmeraldGreen
      TroopType.ENGINEER -> GameColors.OrangeBright
      TroopType.HEAVY_GUARDIAN -> Color(0xFF94A3B8)
      TroopType.SCOUT_RIDER -> GameColors.GoldYellow
      TroopType.STAR_MAGE -> GameColors.RoyalPurple
    }
  Canvas(modifier = Modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    drawCircle(color = bg.copy(alpha = 0.25f), radius = w * 0.48f)
    drawCircle(color = bg, radius = w * 0.36f, center = Offset(w / 2f, h * 0.52f))
    // Cute cartoon visor / eyes
    drawRoundRect(
      color = Color(0xFF0F172A),
      topLeft = Offset(w * 0.31f, h * 0.42f),
      size = Size(w * 0.38f, h * 0.14f),
      cornerRadius = CornerRadius(6f, 6f),
    )
    drawCircle(color = Color.White, radius = w * 0.045f, center = Offset(w * 0.42f, h * 0.49f))
    drawCircle(color = Color.White, radius = w * 0.045f, center = Offset(w * 0.58f, h * 0.49f))

    // Class headgear accent
    when (type) {
      TroopType.FOOT_SOLDIER -> {
        drawArc(
          color = GameColors.CoralRed,
          startAngle = 180f,
          sweepAngle = 180f,
          useCenter = true,
          topLeft = Offset(w * 0.34f, h * 0.12f),
          size = Size(w * 0.32f, h * 0.24f),
        )
      }
      TroopType.ARCHER -> {
        val feather =
          Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.68f, h * 0.24f)
            lineTo(w * 0.42f, h * 0.26f)
            close()
          }
        drawPath(feather, GameColors.GoldYellow)
      }
      TroopType.ENGINEER -> {
        drawRoundRect(
          color = GameColors.GoldYellow,
          topLeft = Offset(w * 0.26f, h * 0.17f),
          size = Size(w * 0.48f, h * 0.16f),
          cornerRadius = CornerRadius(8f, 8f),
        )
      }
      TroopType.HEAVY_GUARDIAN -> {
        drawRoundRect(
          color = GameColors.Turquoise,
          topLeft = Offset(w * 0.43f, h * 0.22f),
          size = Size(w * 0.14f, h * 0.14f),
          cornerRadius = CornerRadius(4f, 4f),
        )
      }
      TroopType.SCOUT_RIDER -> {
        drawCircle(color = GameColors.OrangeBright, radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.18f))
      }
      TroopType.STAR_MAGE -> {
        val hat =
          Path().apply {
            moveTo(w * 0.5f, h * 0.04f)
            lineTo(w * 0.72f, h * 0.28f)
            lineTo(w * 0.28f, h * 0.28f)
            close()
          }
        drawPath(hat, GameColors.GoldYellow)
      }
    }
  }
}

@Composable
fun CartoonBuildingIcon(type: BuildingType, size: Dp = 50.dp) {
  Canvas(modifier = Modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    val baseColor =
      when (type) {
        BuildingType.COMMAND_CITADEL -> GameColors.GoldYellow
        BuildingType.CITIZEN_COTTAGE -> GameColors.SkyBlue
        BuildingType.HERO_BARRACKS -> GameColors.CoralRed
        BuildingType.GOLD_MINE -> GameColors.AmberGold
        BuildingType.RESOURCE_MILL -> GameColors.EmeraldGreen
        BuildingType.CRYSTAL_EXTRACTOR -> GameColors.Turquoise
        BuildingType.RESOURCE_VAULT -> Color(0xFF94A3B8)
        BuildingType.TECH_LABORATORY -> GameColors.RoyalPurple
        BuildingType.DEFENSE_TOWER -> GameColors.OrangeBright
        BuildingType.PROTECTIVE_WALL -> Color(0xFFCBD5E1)
      }
    drawRoundRect(
      color = baseColor.copy(alpha = 0.2f),
      topLeft = Offset.Zero,
      size = Size(w, h),
      cornerRadius = CornerRadius(14f, 14f),
    )
    // Building body
    drawRoundRect(
      color = baseColor,
      topLeft = Offset(w * 0.2f, h * 0.38f),
      size = Size(w * 0.6f, h * 0.44f),
      cornerRadius = CornerRadius(8f, 8f),
    )
    // Cute pitched roof
    val roof =
      Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        lineTo(w * 0.86f, h * 0.42f)
        lineTo(w * 0.14f, h * 0.42f)
        close()
      }
    drawPath(roof, baseColor.copy(alpha = 0.85f))
    // Door / window highlight
    drawRoundRect(
      color = GameColors.MidnightNavy,
      topLeft = Offset(w * 0.41f, h * 0.55f),
      size = Size(w * 0.18f, h * 0.27f),
      cornerRadius = CornerRadius(5f, 5f),
    )
  }
}
