package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActionFeedback
import com.example.data.GameRepository
import com.example.data.local.GameDatabase
import com.example.data.preferences.GamePreferences
import com.example.data.preferences.GamePreferencesRepository
import com.example.engine.BattleEngine
import com.example.model.AchievementItem
import com.example.model.BattleResultSummary
import com.example.model.BattleSimulationState
import com.example.model.BattleStage
import com.example.model.BiomeType
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.model.MissionItem
import com.example.model.PlacedBuilding
import com.example.model.PlayerProfile
import com.example.model.ResourceBundle
import com.example.model.TechNodeState
import com.example.model.TechType
import com.example.model.TroopState
import com.example.model.TroopType
import com.example.model.TutorialStep
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameUiState(
  val isLoading: Boolean = true,
  val player: PlayerProfile = PlayerProfile(),
  val buildings: List<PlacedBuilding> = emptyList(),
  val troops: List<TroopState> = emptyList(),
  val techs: List<TechNodeState> = emptyList(),
  val missions: List<MissionItem> = emptyList(),
  val achievements: List<AchievementItem> = emptyList(),
  val stages: List<BattleStage> = emptyList(),
  val preferences: GamePreferences = GamePreferences(),
  val storageCap: ResourceBundle = ResourceBundle(1200, 1200, 1000, 350),
  val armyHousingUsed: Int = 0,
  val armyHousingCap: Int = 18,
  val armyPower: Int = 0,
  val defensePower: Int = 0,
  val pendingCollectTotal: Int = 0,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
  private val db = GameDatabase.getInstance(application)
  private val prefsRepo = GamePreferencesRepository(application)
  val repository = GameRepository(db.gameDao(), prefsRepo)
  private val battleEngine = BattleEngine()

  private val _selectedBuildingId = MutableStateFlow<Long?>(null)
  val selectedBuildingId: StateFlow<Long?> = _selectedBuildingId.asStateFlow()

  private val _movingBuildingId = MutableStateFlow<Long?>(null)
  val movingBuildingId: StateFlow<Long?> = _movingBuildingId.asStateFlow()

  private val _placingBuildingType = MutableStateFlow<BuildingType?>(null)
  val placingBuildingType: StateFlow<BuildingType?> = _placingBuildingType.asStateFlow()

  private val _selectedEmptyCell = MutableStateFlow<Pair<Int, Int>?>(null)
  val selectedEmptyCell: StateFlow<Pair<Int, Int>?> = _selectedEmptyCell.asStateFlow()

  private val _feedback = MutableStateFlow<ActionFeedback?>(null)
  val feedback: StateFlow<ActionFeedback?> = _feedback.asStateFlow()

  private val _battleState = MutableStateFlow(BattleSimulationState())
  val battleState: StateFlow<BattleSimulationState> = _battleState.asStateFlow()

  private val _lastBattleResult = MutableStateFlow<BattleResultSummary?>(null)
  val lastBattleResult: StateFlow<BattleResultSummary?> = _lastBattleResult.asStateFlow()

  private var battleLoopJob: Job? = null
  private var totalDeployedInCurrentBattle = 0

  val uiState: StateFlow<GameUiState> =
    combine(
      repository.playerFlow,
      repository.buildingsFlow,
      repository.troopsFlow,
      repository.techsFlow,
      repository.missionsFlow,
      repository.achievementsFlow,
      repository.stagesFlow,
      repository.preferencesFlow,
    ) { values ->
      @Suppress("UNCHECKED_CAST")
      val player = values[0] as PlayerProfile
      @Suppress("UNCHECKED_CAST")
      val buildings = values[1] as List<PlacedBuilding>
      @Suppress("UNCHECKED_CAST")
      val troops = values[2] as List<TroopState>
      @Suppress("UNCHECKED_CAST")
      val techs = values[3] as List<TechNodeState>
      @Suppress("UNCHECKED_CAST")
      val missions = values[4] as List<MissionItem>
      @Suppress("UNCHECKED_CAST")
      val achievements = values[5] as List<AchievementItem>
      @Suppress("UNCHECKED_CAST")
      val stages = values[6] as List<BattleStage>
      val prefs = values[7] as GamePreferences

      val usedHousing =
        troops.sumOf { t -> t.count * GameCatalog.troops.getValue(t.type).housingSpace }
      val power =
        troops.sumOf { t ->
          val def = GameCatalog.troops.getValue(t.type)
          t.count * ((def.baseAttack + def.baseHp / 10) * t.level)
        }
      val defPower =
        buildings.sumOf { b ->
          when (b.type) {
            BuildingType.DEFENSE_TOWER -> b.level * 45
            BuildingType.PROTECTIVE_WALL -> b.level * 18
            BuildingType.COMMAND_CITADEL -> b.level * 30
            else -> b.level * 5
          }
        }
      val pendingCollect =
        buildings.sumOf { it.storedGold + it.storedWood + it.storedStone + it.storedCrystal }

      GameUiState(
        isLoading = false,
        player = player,
        buildings = buildings,
        troops = troops,
        techs = techs,
        missions = missions,
        achievements = achievements,
        stages = stages,
        preferences = prefs,
        storageCap = repository.computeStorageCap(buildings),
        armyHousingUsed = usedHousing,
        armyHousingCap = repository.computeArmyCapacity(buildings),
        armyPower = power,
        defensePower = defPower,
        pendingCollectTotal = pendingCollect,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = GameUiState(),
    )

  init {
    viewModelScope.launch {
      repository.ensureInitialized()
      repository.processProductionTick()
      while (isActive) {
        delay(3500L)
        repository.processProductionTick()
      }
    }
  }

  fun clearFeedback() {
    _feedback.value = null
  }

  fun selectBuilding(buildingId: Long?) {
    _selectedBuildingId.value = buildingId
    if (buildingId != null) _selectedEmptyCell.value = null
  }

  fun clearSelectedEmptyCell() {
    _selectedEmptyCell.value = null
  }

  fun startMovingBuilding(buildingId: Long) {
    _movingBuildingId.value = buildingId
    _placingBuildingType.value = null
    _selectedBuildingId.value = null
    _selectedEmptyCell.value = null
    _feedback.value = ActionFeedback(true, "خانه خالی مورد نظر روی نقشه را لمس کن تا ساختمان جابه‌جا شود.")
  }

  fun cancelMovingBuilding() {
    _movingBuildingId.value = null
  }

  fun startPlacingBuilding(type: BuildingType) {
    _placingBuildingType.value = type
    _movingBuildingId.value = null
    _selectedBuildingId.value = null
    _feedback.value = ActionFeedback(true, "یک خانه خالی روی شبکه نقشه برای ساخت ${type.titleFa} لمس کن.")
  }

  fun cancelPlacingBuilding() {
    _placingBuildingType.value = null
  }

  fun placeBuildingAt(type: BuildingType, gridX: Int, gridY: Int) {
    viewModelScope.launch {
      val res = repository.buildBuilding(type, preferredX = gridX, preferredY = gridY)
      _feedback.value = res
      if (res.success) {
        _placingBuildingType.value = null
        _selectedEmptyCell.value = null
      }
    }
  }

  fun handleGridTap(gridX: Int, gridY: Int) {
    val movingId = _movingBuildingId.value
    val placingType = _placingBuildingType.value
    val buildings = uiState.value.buildings
    val tappedBuilding = buildings.firstOrNull { it.gridX == gridX && it.gridY == gridY }

    if (movingId != null) {
      if (tappedBuilding == null) {
        viewModelScope.launch {
          val res = repository.moveBuilding(movingId, gridX, gridY)
          _feedback.value = res
          if (res.success) _movingBuildingId.value = null
        }
      } else if (tappedBuilding.id == movingId) {
        _movingBuildingId.value = null
      } else {
        _feedback.value = ActionFeedback(false, "این خانه پر است؛ یک خانه خالی سبز انتخاب کن.")
      }
      return
    }

    if (placingType != null) {
      if (tappedBuilding == null) {
        placeBuildingAt(placingType, gridX, gridY)
      } else {
        _feedback.value = ActionFeedback(false, "این خانه اشغال شده است؛ خانه خالی دیگری را لمس کن.")
      }
      return
    }

    if (tappedBuilding != null) {
      _selectedBuildingId.value = tappedBuilding.id
      _selectedEmptyCell.value = null
    } else {
      _selectedBuildingId.value = null
      _selectedEmptyCell.value = gridX to gridY
    }
  }

  fun collectAllResources() {
    viewModelScope.launch {
      _feedback.value = repository.collectAllResources()
    }
  }

  fun collectBuilding(buildingId: Long) {
    viewModelScope.launch {
      _feedback.value = repository.collectBuilding(buildingId)
    }
  }

  fun buildBuilding(type: BuildingType) {
    viewModelScope.launch {
      _feedback.value = repository.buildBuilding(type)
    }
  }

  fun upgradeBuilding(buildingId: Long) {
    viewModelScope.launch {
      _feedback.value = repository.upgradeBuilding(buildingId)
    }
  }

  fun trainTroop(type: TroopType, amount: Int = 1) {
    viewModelScope.launch {
      _feedback.value = repository.trainTroop(type, amount)
    }
  }

  fun upgradeTroop(type: TroopType) {
    viewModelScope.launch {
      _feedback.value = repository.upgradeTroop(type)
    }
  }

  fun upgradeTech(type: TechType) {
    viewModelScope.launch {
      _feedback.value = repository.upgradeTech(type)
    }
  }

  fun selectBiome(biome: BiomeType, newKingdomName: String? = null) {
    viewModelScope.launch {
      _feedback.value = repository.selectBiome(biome, newKingdomName)
    }
  }

  fun claimMission(missionId: String) {
    viewModelScope.launch {
      _feedback.value = repository.claimMission(missionId)
    }
  }

  fun claimAchievement(achievementId: String) {
    viewModelScope.launch {
      _feedback.value = repository.claimAchievement(achievementId)
    }
  }

  fun skipTutorial() {
    viewModelScope.launch {
      repository.setTutorialStep(TutorialStep.COMPLETED)
    }
  }

  fun replayTutorial() {
    viewModelScope.launch {
      repository.setTutorialStep(TutorialStep.WELCOME_COLLECT)
      _feedback.value = ActionFeedback(true, "آموزش تعاملی از ابتدا فعال شد!")
    }
  }

  fun toggleSound(enabled: Boolean) {
    viewModelScope.launch { repository.setSoundEnabled(enabled) }
  }

  fun toggleAnimations(enabled: Boolean) {
    viewModelScope.launch { repository.setAnimationsEnabled(enabled) }
  }

  fun toggleHighContrast(enabled: Boolean) {
    viewModelScope.launch { repository.setHighContrastMode(enabled) }
  }

  fun resetGameProgress() {
    viewModelScope.launch {
      repository.resetAllGameData()
      _selectedBuildingId.value = null
      _movingBuildingId.value = null
      _feedback.value = ActionFeedback(true, "پیشرفت بازی با موفقیت بازنشانی شد.")
    }
  }

  fun startCampaignBattle(stage: BattleStage, squad: Map<TroopType, Int>): Boolean {
    val cleanSquad = squad.filterValues { it > 0 }
    if (cleanSquad.isEmpty()) {
      _feedback.value = ActionFeedback(false, "حداقل یک نیرو برای نبرد انتخاب کن!")
      return false
    }
    totalDeployedInCurrentBattle = cleanSquad.values.sum()
    viewModelScope.launch {
      repository.consumeDeployedTroops(cleanSquad)
    }
    val initial = battleEngine.createCampaignBattle(stage, cleanSquad)
    _battleState.value = initial
    _lastBattleResult.value = null
    launchBattleLoop()
    return true
  }

  fun startDefenseTestBattle() {
    val state = uiState.value
    val citadelLvl = state.buildings.firstOrNull { it.type == BuildingType.COMMAND_CITADEL }?.level ?: 1
    totalDeployedInCurrentBattle = 0
    val initial =
      battleEngine.createBaseDefenseSimulation(
        playerBuildings = state.buildings,
        biome = state.player.activeBiome,
        techs = state.techs,
        citadelLevel = citadelLvl,
      )
    _battleState.value = initial
    _lastBattleResult.value = null
    launchBattleLoop()
  }

  fun selectDeployTroop(type: TroopType) {
    _battleState.value = _battleState.value.copy(selectedDeployTroop = type)
  }

  fun deployTroopAt(gridX: Float, gridY: Float) {
    val current = _battleState.value
    val selected = current.selectedDeployTroop ?: current.remainingToDeploy.keys.firstOrNull() ?: return
    val updated =
      battleEngine.deployUnit(
        state = current,
        troopType = selected,
        gridX = gridX,
        gridY = gridY,
        troopStates = uiState.value.troops,
        techs = uiState.value.techs,
      )
    _battleState.value = updated
  }

  fun deployAllRemainingSquad() {
    var current = _battleState.value
    if (current.isDefenseTest || current.isFinished) return
    val entries = current.remainingToDeploy.entries.toList()
    var slot = 0
    for ((type, count) in entries) {
      repeat(count) {
        val gx = 1.0f + (slot % 6) * 1.15f
        val gy = 6.2f + (slot / 6) * 0.45f
        current =
          battleEngine.deployUnit(
            state = current,
            troopType = type,
            gridX = gx,
            gridY = gy,
            troopStates = uiState.value.troops,
            techs = uiState.value.techs,
          )
        slot++
      }
    }
    _battleState.value = current
  }

  fun stopBattleEarly() {
    battleLoopJob?.cancel()
    _battleState.value = BattleSimulationState()
  }

  private fun launchBattleLoop() {
    battleLoopJob?.cancel()
    battleLoopJob =
      viewModelScope.launch {
        while (isActive && _battleState.value.isRunning && !_battleState.value.isFinished) {
          delay(50L)
          val next =
            battleEngine.step(
              state = _battleState.value,
              dt = 0.05f,
              techs = uiState.value.techs,
              totalDeployedCount = totalDeployedInCurrentBattle,
            )
          _battleState.value = next
          if (next.isFinished && next.result != null) {
            _lastBattleResult.value = next.result
            repository.recordBattleCompletion(next.result)
            break
          }
        }
      }
  }
}
