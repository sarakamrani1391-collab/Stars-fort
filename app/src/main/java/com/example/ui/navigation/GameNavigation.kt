package com.example.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.TutorialStep
import com.example.ui.screens.BattleArenaScreen
import com.example.ui.screens.BattleResultScreen
import com.example.ui.screens.BattleSelectScreen
import com.example.ui.screens.BuildingsScreen
import com.example.ui.screens.MainMapScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.RealmSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StartScreen
import com.example.ui.screens.TroopsTechScreen
import com.example.ui.theme.DejSetareganTheme
import com.example.ui.theme.GameColors
import com.example.ui.viewmodel.GameViewModel

object GameRoutes {
  const val START = "start"
  const val REALMS = "realms"
  const val MAP = "map"
  const val BUILDINGS = "buildings"
  const val TROOPS = "troops"
  const val BATTLE_SELECT = "battle_select"
  const val BATTLE_ARENA = "battle_arena"
  const val BATTLE_RESULT = "battle_result"
  const val MISSIONS = "missions"
  const val SETTINGS = "settings"
}

private data class HubTab(
  val route: String,
  val labelFa: String,
  val icon: ImageVector,
  val testTag: String,
)

private val hubTabs =
  listOf(
    HubTab(GameRoutes.MAP, "نقشه", Icons.Rounded.Map, "nav_map"),
    HubTab(GameRoutes.BUILDINGS, "ساختمان‌ها", Icons.Rounded.Apartment, "nav_buildings"),
    HubTab(GameRoutes.TROOPS, "نیروها", Icons.Rounded.Groups, "nav_troops"),
    HubTab(GameRoutes.BATTLE_SELECT, "نبردها", Icons.Rounded.SportsMartialArts, "nav_battle"),
    HubTab(GameRoutes.MISSIONS, "مأموریت‌ها", Icons.Rounded.EmojiEvents, "nav_missions"),
  )

@Composable
fun DejSetareganApp(viewModel: GameViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val selectedBuildingId by viewModel.selectedBuildingId.collectAsStateWithLifecycle()
  val movingBuildingId by viewModel.movingBuildingId.collectAsStateWithLifecycle()
  val selectedEmptyCell by viewModel.selectedEmptyCell.collectAsStateWithLifecycle()
  val placingBuildingType by viewModel.placingBuildingType.collectAsStateWithLifecycle()
  val feedback by viewModel.feedback.collectAsStateWithLifecycle()
  val battleState by viewModel.battleState.collectAsStateWithLifecycle()
  val lastBattleResult by viewModel.lastBattleResult.collectAsStateWithLifecycle()

  val navController = rememberNavController()
  val snackbarHostState = remember { SnackbarHostState() }
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route

  LaunchedEffect(feedback) {
    feedback?.let {
      snackbarHostState.showSnackbar(it.messageFa)
      viewModel.clearFeedback()
    }
  }

  LaunchedEffect(battleState.isFinished, battleState.result) {
    if (battleState.isFinished && battleState.result != null && currentRoute == GameRoutes.BATTLE_ARENA) {
      navController.navigate(GameRoutes.BATTLE_RESULT) {
        popUpTo(GameRoutes.BATTLE_ARENA) { inclusive = true }
      }
    }
  }

  fun navigateToHubTab(route: String) {
    navController.navigate(route) {
      popUpTo(GameRoutes.MAP) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  DejSetareganTheme(
    activeBiome = uiState.player.activeBiome,
    highContrast = uiState.preferences.highContrastMode,
  ) {
    val showHubNav = currentRoute in hubTabs.map { it.route }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useNavRail = maxWidth >= 600.dp

      Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
          SnackbarHost(snackbarHostState) { data ->
            Snackbar(
              snackbarData = data,
              containerColor = GameColors.MidnightNavy,
              contentColor = GameColors.GoldYellow,
            )
          }
        },
        bottomBar = {
          if (showHubNav && !useNavRail) {
            NavigationBar(containerColor = GameColors.MidnightNavy) {
              hubTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationBarItem(
                  selected = selected,
                  onClick = { navigateToHubTab(tab.route) },
                  icon = { Icon(tab.icon, contentDescription = tab.labelFa) },
                  label = { Text(tab.labelFa, maxLines = 1) },
                  colors =
                    NavigationBarItemDefaults.colors(
                      selectedIconColor = GameColors.DeepNavy,
                      selectedTextColor = GameColors.GoldYellow,
                      indicatorColor = GameColors.GoldYellow,
                      unselectedIconColor = GameColors.OffWhite.copy(alpha = 0.7f),
                      unselectedTextColor = GameColors.OffWhite.copy(alpha = 0.7f),
                    ),
                  modifier = Modifier.testTag(tab.testTag),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          if (showHubNav && useNavRail) {
            NavigationRail(containerColor = GameColors.MidnightNavy) {
              hubTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationRailItem(
                  selected = selected,
                  onClick = { navigateToHubTab(tab.route) },
                  icon = { Icon(tab.icon, contentDescription = tab.labelFa) },
                  label = { Text(tab.labelFa) },
                  modifier = Modifier.testTag(tab.testTag),
                )
              }
            }
          }

          NavHost(
            navController = navController,
            startDestination = GameRoutes.START,
            modifier = Modifier.weight(1f),
          ) {
            composable(GameRoutes.START) {
              StartScreen(
                uiState = uiState,
                onContinueGame = {
                  navController.navigate(GameRoutes.MAP) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                  }
                },
                onSelectRealm = { navController.navigate(GameRoutes.REALMS) },
                onOpenSettings = { navController.navigate(GameRoutes.SETTINGS) },
              )
            }

            composable(GameRoutes.REALMS) {
              RealmSelectScreen(
                uiState = uiState,
                onSelectBiome = { biome, name ->
                  viewModel.selectBiome(biome, name)
                },
                onBack = { navController.popBackStack() },
              )
            }

            composable(GameRoutes.MAP) {
              MainMapScreen(
                uiState = uiState,
                selectedBuildingId = selectedBuildingId,
                movingBuildingId = movingBuildingId,
                selectedEmptyCell = selectedEmptyCell,
                placingBuildingType = placingBuildingType,
                onGridTap = viewModel::handleGridTap,
                onCollectAll = viewModel::collectAllResources,
                onCollectBuilding = viewModel::collectBuilding,
                onUpgradeBuilding = viewModel::upgradeBuilding,
                onPlaceBuildingAt = viewModel::placeBuildingAt,
                onStartMoveBuilding = viewModel::startMovingBuilding,
                onCancelMove = viewModel::cancelMovingBuilding,
                onCancelPlace = viewModel::cancelPlacingBuilding,
                onDismissEmptyCell = viewModel::clearSelectedEmptyCell,
                onDismissSelected = { viewModel.selectBuilding(null) },
                onStartDefenseTest = {
                  viewModel.startDefenseTestBattle()
                  navController.navigate(GameRoutes.BATTLE_ARENA)
                },
                onOpenRealms = { navController.navigate(GameRoutes.REALMS) },
                onOpenSettings = { navController.navigate(GameRoutes.SETTINGS) },
                onSkipTutorial = viewModel::skipTutorial,
                onNavigateForTutorial = { step ->
                  when (step) {
                    TutorialStep.WELCOME_COLLECT -> viewModel.collectAllResources()
                    TutorialStep.BUILD_STRUCTURE -> navigateToHubTab(GameRoutes.BUILDINGS)
                    TutorialStep.TRAIN_TROOP -> navigateToHubTab(GameRoutes.TROOPS)
                    TutorialStep.FIRST_BATTLE -> navigateToHubTab(GameRoutes.BATTLE_SELECT)
                    TutorialStep.COMPLETED -> Unit
                  }
                },
              )
            }

            composable(GameRoutes.BUILDINGS) {
              BuildingsScreen(
                uiState = uiState,
                onBuildBuilding = viewModel::buildBuilding,
                onUpgradeBuilding = viewModel::upgradeBuilding,
                onMoveOnMap = { buildingId ->
                  viewModel.startMovingBuilding(buildingId)
                  navigateToHubTab(GameRoutes.MAP)
                },
              )
            }

            composable(GameRoutes.TROOPS) {
              TroopsTechScreen(
                uiState = uiState,
                onTrainTroop = viewModel::trainTroop,
                onUpgradeTroop = viewModel::upgradeTroop,
                onUpgradeTech = viewModel::upgradeTech,
              )
            }

            composable(GameRoutes.BATTLE_SELECT) {
              BattleSelectScreen(
                uiState = uiState,
                onStartBattle = { stage, squad ->
                  if (viewModel.startCampaignBattle(stage, squad)) {
                    navController.navigate(GameRoutes.BATTLE_ARENA)
                  }
                },
                onGoToBarracks = { navigateToHubTab(GameRoutes.TROOPS) },
              )
            }

            composable(GameRoutes.BATTLE_ARENA) {
              BattleArenaScreen(
                battleState = battleState,
                onSelectTroop = viewModel::selectDeployTroop,
                onDeployAt = viewModel::deployTroopAt,
                onDeployAll = viewModel::deployAllRemainingSquad,
                onExitBattle = {
                  viewModel.stopBattleEarly()
                  navController.popBackStack()
                },
              )
            }

            composable(GameRoutes.BATTLE_RESULT) {
              BattleResultScreen(
                result = lastBattleResult,
                onBackToMap = { navigateToHubTab(GameRoutes.MAP) },
                onBackToBattles = { navigateToHubTab(GameRoutes.BATTLE_SELECT) },
              )
            }

            composable(GameRoutes.MISSIONS) {
              MissionsScreen(
                uiState = uiState,
                onClaimMission = viewModel::claimMission,
                onClaimAchievement = viewModel::claimAchievement,
              )
            }

            composable(GameRoutes.SETTINGS) {
              SettingsScreen(
                uiState = uiState,
                onToggleSound = viewModel::toggleSound,
                onToggleAnimations = viewModel::toggleAnimations,
                onToggleHighContrast = viewModel::toggleHighContrast,
                onReplayTutorial = viewModel::replayTutorial,
                onResetGame = viewModel::resetGameProgress,
                onBack = { navController.popBackStack() },
              )
            }
          }
        }
      }
    }
  }
}
