package com.example.data

import com.example.data.local.AchievementEntity
import com.example.data.local.BuildingEntity
import com.example.data.local.GameDao
import com.example.data.local.MissionEntity
import com.example.data.local.PlayerEntity
import com.example.data.local.StageProgressEntity
import com.example.data.local.TechEntity
import com.example.data.local.TroopEntity
import com.example.data.preferences.GamePreferences
import com.example.data.preferences.GamePreferencesRepository
import com.example.model.AchievementItem
import com.example.model.BattleResultSummary
import com.example.model.BattleStage
import com.example.model.BiomeType
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.model.MissionCategory
import com.example.model.MissionItem
import com.example.model.PlacedBuilding
import com.example.model.PlayerProfile
import com.example.model.ResourceBundle
import com.example.model.TechNodeState
import com.example.model.TechType
import com.example.model.TroopState
import com.example.model.TroopType
import com.example.model.TutorialStep
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ActionFeedback(val success: Boolean, val messageFa: String)

class GameRepository(
  private val dao: GameDao,
  private val prefs: GamePreferencesRepository,
) {
  private val mutex = Mutex()

  val preferencesFlow: Flow<GamePreferences> = prefs.preferences

  val playerFlow: Flow<PlayerProfile> =
    dao.observePlayer().map { entity -> entity?.toDomain() ?: PlayerProfile() }

  val buildingsFlow: Flow<List<PlacedBuilding>> =
    dao.observeBuildings().map { list -> list.map { it.toDomain() } }

  val troopsFlow: Flow<List<TroopState>> =
    dao.observeTroops().map { list ->
      val map = list.associateBy { it.type }
      TroopType.entries.map { type ->
        val e = map[type.name]
        TroopState(type = type, level = e?.level ?: 1, count = e?.count ?: 0)
      }
    }

  val techsFlow: Flow<List<TechNodeState>> =
    dao.observeTechs().map { list ->
      val map = list.associateBy { it.type }
      TechType.entries.map { type ->
        val e = map[type.name]
        TechNodeState(type = type, level = e?.level ?: 0)
      }
    }

  val missionsFlow: Flow<List<MissionItem>> =
    dao.observeMissions().map { list -> list.map { it.toDomain() } }

  val achievementsFlow: Flow<List<AchievementItem>> =
    dao.observeAchievements().map { list -> list.map { it.toDomain() } }

  val stagesFlow: Flow<List<BattleStage>> =
    combine(dao.observeStages(), playerFlow) { progressList, player ->
      val progressMap = progressList.associateBy { it.stageId }
      GameCatalog.battleStages.map { stage ->
        val prog = progressMap[stage.id]
        val prevPassed =
          if (stage.id == 1) true else (progressMap[stage.id - 1]?.starsEarned ?: 0) > 0
        val unlockedByLevel = player.level >= stage.biome.unlockLevel
        stage.copy(
          starsEarned = prog?.starsEarned ?: 0,
          unlocked = (prog?.unlocked == true) || prevPassed || unlockedByLevel && stage.id % 2 == 1 && prevPassed,
        )
      }
    }

  suspend fun ensureInitialized() {
    mutex.withLock {
      if (dao.getPlayerOnce() == null) {
        seedInitialData()
      }
      refreshDailyMissionsIfNeeded()
    }
  }

  private suspend fun seedInitialData() {
    val defaultPlayer = PlayerProfile()
    dao.upsertPlayer(defaultPlayer.toEntity())
    dao.insertBuildings(
      GameCatalog.initialPlayerBuildings().map {
        BuildingEntity(
          id = it.id,
          type = it.type.name,
          level = it.level,
          gridX = it.gridX,
          gridY = it.gridY,
          storedGold = it.storedGold,
          storedWood = it.storedWood,
          storedStone = it.storedStone,
          storedCrystal = it.storedCrystal,
        )
      }
    )
    dao.upsertTroops(
      TroopType.entries.map { type ->
        val initialCount =
          when (type) {
            TroopType.FOOT_SOLDIER -> 3
            TroopType.ARCHER -> 2
            else -> 0
          }
        TroopEntity(type = type.name, level = 1, count = initialCount)
      }
    )
    dao.upsertTechs(TechType.entries.map { TechEntity(type = it.name, level = 0) })
    dao.upsertStages(
      GameCatalog.battleStages.map { stage ->
        StageProgressEntity(stageId = stage.id, starsEarned = 0, unlocked = stage.id == 1)
      }
    )
    dao.upsertAchievements(defaultAchievements())
    dao.upsertMissions(buildDailyMissions())
    prefs.setLastProductionTickMs(System.currentTimeMillis())
    prefs.setLastMissionDate(todayKey())
  }

  private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

  private suspend fun refreshDailyMissionsIfNeeded() {
    val currentPrefs = prefs.getCurrent()
    val today = todayKey()
    if (currentPrefs.lastMissionDate != today || dao.getMissionsOnce().isEmpty()) {
      dao.clearMissions()
      dao.upsertMissions(buildDailyMissions())
      prefs.setLastMissionDate(today)
    }
  }

  suspend fun processProductionTick() {
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return
      val now = System.currentTimeMillis()
      val currentPrefs = prefs.getCurrent()
      val lastTick = if (currentPrefs.lastProductionTickMs == 0L) now else currentPrefs.lastProductionTickMs
      val elapsedSec = ((now - lastTick) / 1000f).coerceIn(0f, 14400f)
      if (elapsedSec < 2f) return

      val biome = BiomeType.entries.firstOrNull { it.name == player.activeBiome } ?: BiomeType.EMERALD_MEADOW
      val harvestLevel = dao.getTechsOnce().firstOrNull { it.type == TechType.HARVEST_BOOST.name }?.level ?: 0
      val techMult = 1f + harvestLevel * 0.15f

      val buildings = dao.getBuildingsOnce()
      val updated =
        buildings.map { b ->
          val type = BuildingType.valueOf(b.type)
          val maxBuffer = 60 + b.level * 55
          when (type) {
            BuildingType.GOLD_MINE -> {
              val gain = (elapsedSec * (0.45f + b.level * 0.25f) * biome.goldMultiplier * techMult).toInt()
              b.copy(storedGold = (b.storedGold + gain).coerceAtMost(maxBuffer))
            }
            BuildingType.RESOURCE_MILL -> {
              val wGain = (elapsedSec * (0.40f + b.level * 0.22f) * biome.woodMultiplier * techMult).toInt()
              val sGain = (elapsedSec * (0.30f + b.level * 0.18f) * biome.stoneMultiplier * techMult).toInt()
              b.copy(
                storedWood = (b.storedWood + wGain).coerceAtMost(maxBuffer),
                storedStone = (b.storedStone + sGain).coerceAtMost(maxBuffer),
              )
            }
            BuildingType.CRYSTAL_EXTRACTOR -> {
              val cGain = (elapsedSec * (0.14f + b.level * 0.08f) * biome.crystalMultiplier * techMult).toInt()
              b.copy(storedCrystal = (b.storedCrystal + cGain).coerceAtMost(maxBuffer / 2))
            }
            else -> b
          }
        }
      dao.updateBuildings(updated)
      prefs.setLastProductionTickMs(now)
    }
  }

  fun computeStorageCap(buildings: List<PlacedBuilding>): ResourceBundle {
    val citadelLvl = buildings.firstOrNull { it.type == BuildingType.COMMAND_CITADEL }?.level ?: 1
    val vaultSum = buildings.filter { it.type == BuildingType.RESOURCE_VAULT }.sumOf { it.level }
    val base = 900 + citadelLvl * 450 + vaultSum * 420
    return ResourceBundle(
      gold = base,
      wood = base,
      stone = (base * 0.85f).toInt(),
      crystal = 240 + citadelLvl * 110 + vaultSum * 110,
    )
  }

  fun computeArmyCapacity(buildings: List<PlacedBuilding>): Int {
    val citadelLvl = buildings.firstOrNull { it.type == BuildingType.COMMAND_CITADEL }?.level ?: 1
    val barracksSum = buildings.filter { it.type == BuildingType.HERO_BARRACKS }.sumOf { it.level }
    return 10 + citadelLvl * 4 + barracksSum * 6
  }

  suspend fun collectAllResources(): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val buildings = dao.getBuildingsOnce()
      var gainGold = 0
      var gainWood = 0
      var gainStone = 0
      var gainCrystal = 0
      val cleared =
        buildings.map { b ->
          gainGold += b.storedGold
          gainWood += b.storedWood
          gainStone += b.storedStone
          gainCrystal += b.storedCrystal
          b.copy(storedGold = 0, storedWood = 0, storedStone = 0, storedCrystal = 0)
        }
      val total = gainGold + gainWood + gainStone + gainCrystal
      if (total == 0) {
        return ActionFeedback(false, "هنوز منبعی آماده برداشت نیست؛ چند لحظه صبر کن!")
      }
      val cap = computeStorageCap(buildings.map { it.toDomain() })
      val updatedPlayer =
        player.copy(
          gold = (player.gold + gainGold).coerceAtMost(cap.gold),
          wood = (player.wood + gainWood).coerceAtMost(cap.wood),
          stone = (player.stone + gainStone).coerceAtMost(cap.stone),
          crystal = (player.crystal + gainCrystal).coerceAtMost(cap.crystal),
          totalResourcesCollected = player.totalResourcesCollected + total,
        )
      dao.updateBuildings(cleared)
      dao.upsertPlayer(updatedPlayer)
      incrementMission(MissionCategory.COLLECT_RESOURCES, total)
      syncAchievements(updatedPlayer)
      advanceTutorialIfAt(TutorialStep.WELCOME_COLLECT, TutorialStep.BUILD_STRUCTURE)
      ActionFeedback(true, "منابع جمع‌آوری شد: +$gainGold طلا، +$gainWood چوب، +$gainStone سنگ، +$gainCrystal کریستال")
    }

  suspend fun collectBuilding(buildingId: Long): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val buildings = dao.getBuildingsOnce()
      val target = buildings.firstOrNull { it.id == buildingId } ?: return ActionFeedback(false, "ساختمان پیدا نشد.")
      val total = target.storedGold + target.storedWood + target.storedStone + target.storedCrystal
      if (total == 0) return ActionFeedback(false, "مخزن این ساختمان فعلاً خالی است.")

      val cap = computeStorageCap(buildings.map { it.toDomain() })
      val updatedPlayer =
        player.copy(
          gold = (player.gold + target.storedGold).coerceAtMost(cap.gold),
          wood = (player.wood + target.storedWood).coerceAtMost(cap.wood),
          stone = (player.stone + target.storedStone).coerceAtMost(cap.stone),
          crystal = (player.crystal + target.storedCrystal).coerceAtMost(cap.crystal),
          totalResourcesCollected = player.totalResourcesCollected + total,
        )
      dao.updateBuilding(target.copy(storedGold = 0, storedWood = 0, storedStone = 0, storedCrystal = 0))
      dao.upsertPlayer(updatedPlayer)
      incrementMission(MissionCategory.COLLECT_RESOURCES, total)
      syncAchievements(updatedPlayer)
      advanceTutorialIfAt(TutorialStep.WELCOME_COLLECT, TutorialStep.BUILD_STRUCTURE)
      ActionFeedback(true, "منابع ساختمان برداشت شد (+$total).")
    }

  suspend fun buildBuilding(type: BuildingType, preferredX: Int? = null, preferredY: Int? = null): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "خطا در بارگذاری پایگاه.")
      val buildings = dao.getBuildingsOnce()
      val existingCount = buildings.count { it.type == type.name }
      if (existingCount >= type.maxCount) {
        return ActionFeedback(false, "حداکثر تعداد مجاز ${type.titleFa} ساخته شده است.")
      }
      val biome = BiomeType.valueOf(player.activeBiome)
      val cost = GameCatalog.buildingBuildCost(type, existingCount, biome)
      val bundle = ResourceBundle(player.gold, player.wood, player.stone, player.crystal)
      if (!bundle.canAfford(cost)) {
        return ActionFeedback(false, "منابع کافی نیست! منابع بیشتری جمع‌آوری کن.")
      }

      val occupied = buildings.map { it.gridX to it.gridY }.toSet()
      val spot =
        if (preferredX != null && preferredY != null && (preferredX to preferredY) !in occupied) {
          preferredX to preferredY
        } else {
          findFreeGridCell(occupied)
        }
          ?: return ActionFeedback(false, "فضای خالی روی نقشه باقی نمانده است!")

      dao.insertBuilding(
        BuildingEntity(
          type = type.name,
          level = 1,
          gridX = spot.first,
          gridY = spot.second,
        )
      )
      val updatedPlayer =
        grantXpAndDeduct(
          player = player.copy(totalBuildingsBuilt = player.totalBuildingsBuilt + 1),
          cost = cost,
          xpGain = 45,
        )
      dao.upsertPlayer(updatedPlayer)
      incrementMission(MissionCategory.BUILD_OR_UPGRADE, 1)
      syncAchievements(updatedPlayer)
      advanceTutorialIfAt(TutorialStep.BUILD_STRUCTURE, TutorialStep.TRAIN_TROOP)
      ActionFeedback(true, "${type.titleFa} با موفقیت ساخته شد!")
    }

  suspend fun moveBuilding(buildingId: Long, newX: Int, newY: Int): ActionFeedback =
    mutex.withLock {
      if (newX !in 0 until GameCatalog.GRID_SIZE || newY !in 0 until GameCatalog.GRID_SIZE) {
        return ActionFeedback(false, "موقعیت خارج از محدوده نقشه است.")
      }
      val buildings = dao.getBuildingsOnce()
      val target = buildings.firstOrNull { it.id == buildingId } ?: return ActionFeedback(false, "ساختمان یافت نشد.")
      val occupiedByOther = buildings.any { it.id != buildingId && it.gridX == newX && it.gridY == newY }
      if (occupiedByOther) {
        return ActionFeedback(false, "این خانه از نقشه توسط ساختمان دیگری اشغال شده است.")
      }
      dao.updateBuilding(target.copy(gridX = newX, gridY = newY))
      ActionFeedback(true, "${BuildingType.valueOf(target.type).titleFa} جابه‌جا شد.")
    }

  suspend fun upgradeBuilding(buildingId: Long): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val buildings = dao.getBuildingsOnce()
      val target = buildings.firstOrNull { it.id == buildingId } ?: return ActionFeedback(false, "ساختمان یافت نشد.")
      val type = BuildingType.valueOf(target.type)
      if (target.level >= type.maxLevel) {
        return ActionFeedback(false, "${type.titleFa} به حداکثر سطح رسیده است.")
      }
      val citadelLevel =
        buildings.firstOrNull { it.type == BuildingType.COMMAND_CITADEL.name }?.level ?: 1
      if (type != BuildingType.COMMAND_CITADEL && target.level >= citadelLevel) {
        return ActionFeedback(false, "ابتدا مرکز فرماندهی را به سطح ${target.level + 1} ارتقا بده!")
      }
      val cost = GameCatalog.buildingUpgradeCost(type, target.level)
      val wallet = ResourceBundle(player.gold, player.wood, player.stone, player.crystal)
      if (!wallet.canAfford(cost)) {
        return ActionFeedback(false, "منابع برای ارتقا کافی نیست.")
      }
      dao.updateBuilding(target.copy(level = target.level + 1))
      val updatedPlayer = grantXpAndDeduct(player, cost, xpGain = 65 * target.level)
      dao.upsertPlayer(updatedPlayer)
      incrementMission(MissionCategory.BUILD_OR_UPGRADE, 1)
      syncAchievements(updatedPlayer)
      advanceTutorialIfAt(TutorialStep.BUILD_STRUCTURE, TutorialStep.TRAIN_TROOP)
      ActionFeedback(true, "${type.titleFa} به سطح ${target.level + 1} ارتقا یافت!")
    }

  suspend fun trainTroop(type: TroopType, amount: Int = 1): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val buildings = dao.getBuildingsOnce()
      val citadelLvl = buildings.firstOrNull { it.type == BuildingType.COMMAND_CITADEL.name }?.level ?: 1
      val def = GameCatalog.troops.getValue(type)
      if (citadelLvl < def.unlockCitadelLevel) {
        return ActionFeedback(false, "برای ${type.titleFa} مرکز فرماندهی سطح ${def.unlockCitadelLevel} لازم است.")
      }
      val troops = dao.getTroopsOnce()
      val currentHousing =
        troops.sumOf { t ->
          val tt = TroopType.valueOf(t.type)
          t.count * GameCatalog.troops.getValue(tt).housingSpace
        }
      val maxHousing = computeArmyCapacity(buildings.map { it.toDomain() })
      if (currentHousing + def.housingSpace * amount > maxHousing) {
        return ActionFeedback(false, "ظرفیت پادگان پر است! خانه یا پادگان بساز و ارتقا بده.")
      }
      val totalCost = def.trainCost.scaled(amount.toFloat())
      val wallet = ResourceBundle(player.gold, player.wood, player.stone, player.crystal)
      if (!wallet.canAfford(totalCost)) {
        return ActionFeedback(false, "منابع کافی برای آموزش ${type.titleFa} وجود ندارد.")
      }
      val existing = troops.firstOrNull { it.type == type.name } ?: TroopEntity(type.name, 1, 0)
      dao.upsertTroop(existing.copy(count = existing.count + amount))
      val updatedPlayer =
        grantXpAndDeduct(
          player = player.copy(totalTroopsTrained = player.totalTroopsTrained + amount),
          cost = totalCost,
          xpGain = 15 * amount,
        )
      dao.upsertPlayer(updatedPlayer)
      incrementMission(MissionCategory.TRAIN_TROOPS, amount)
      syncAchievements(updatedPlayer)
      advanceTutorialIfAt(TutorialStep.TRAIN_TROOP, TutorialStep.FIRST_BATTLE)
      ActionFeedback(true, "$amount ${type.titleFa} به ارتش اضافه شد!")
    }

  suspend fun upgradeTroop(type: TroopType): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val buildings = dao.getBuildingsOnce()
      val labLevel = buildings.firstOrNull { it.type == BuildingType.TECH_LABORATORY.name }?.level ?: 0
      val troop = dao.getTroopsOnce().firstOrNull { it.type == type.name } ?: TroopEntity(type.name, 1, 0)
      if (labLevel == 0 && troop.level >= 1) {
        // Allow level 1 -> 2 even if lab isn't built yet? Better: require lab for lvl 2+ or build lab!
        // Let's keep it accessible: level 2 requires lab level 1 or allow 1 upgrade free, wait: lab makes it meaningful!
        return ActionFeedback(false, "برای ارتقای نیرو ابتدا «آزمایشگاه جادو» را بساز!")
      }
      if (troop.level >= 5) return ActionFeedback(false, "${type.titleFa} در حداکثر سطح است.")
      val cost = GameCatalog.troopUpgradeCost(type, troop.level)
      val wallet = ResourceBundle(player.gold, player.wood, player.stone, player.crystal)
      if (!wallet.canAfford(cost)) {
        return ActionFeedback(false, "منابع کافی برای ارتقای ${type.titleFa} نیست.")
      }
      dao.upsertTroop(troop.copy(level = troop.level + 1))
      val updatedPlayer = grantXpAndDeduct(player, cost, xpGain = 80)
      dao.upsertPlayer(updatedPlayer)
      incrementMission(MissionCategory.RESEARCH_TECH, 1)
      ActionFeedback(true, "${type.titleFa} به سطح ${troop.level + 1} ارتقا پیدا کرد!")
    }

  suspend fun upgradeTech(type: TechType): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val tech = dao.getTechsOnce().firstOrNull { it.type == type.name } ?: TechEntity(type.name, 0)
      if (tech.level >= type.maxLevel) {
        return ActionFeedback(false, "${type.titleFa} به حداکثر سطح رسیده است.")
      }
      val cost = GameCatalog.techUpgradeCost(type, tech.level)
      val wallet = ResourceBundle(player.gold, player.wood, player.stone, player.crystal)
      if (!wallet.canAfford(cost)) {
        return ActionFeedback(false, "منابع یا کریستال کافی برای پژوهش وجود ندارد.")
      }
      dao.upsertTech(tech.copy(level = tech.level + 1))
      val updatedPlayer = grantXpAndDeduct(player, cost, xpGain = 90)
      dao.upsertPlayer(updatedPlayer)
      incrementMission(MissionCategory.RESEARCH_TECH, 1)
      ActionFeedback(true, "فناوری «${type.titleFa}» به سطح ${tech.level + 1} رسید!")
    }

  suspend fun selectBiome(biome: BiomeType, kingdomName: String? = null): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      if (player.level < biome.unlockLevel) {
        return ActionFeedback(false, "برای ورود به ${biome.titleFa} به سطح ${biome.unlockLevel} نیاز داری!")
      }
      val cleanName = kingdomName?.trim()?.takeIf { it.isNotEmpty() } ?: player.kingdomName
      dao.upsertPlayer(player.copy(activeBiome = biome.name, kingdomName = cleanName))
      ActionFeedback(true, "قلمرو فعال به «${biome.titleFa}» تغییر کرد!")
    }

  suspend fun consumeDeployedTroops(deployedCounts: Map<TroopType, Int>) {
    mutex.withLock {
      val troops = dao.getTroopsOnce()
      val updated =
        troops.map { t ->
          val type = TroopType.valueOf(t.type)
          val used = deployedCounts[type] ?: 0
          t.copy(count = (t.count - used).coerceAtLeast(0))
        }
      dao.upsertTroops(updated)
    }
  }

  suspend fun recordBattleCompletion(summary: BattleResultSummary) {
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return
      val buildings = dao.getBuildingsOnce().map { it.toDomain() }
      val cap = computeStorageCap(buildings)

      var updatedPlayer =
        player.copy(
          gold = (player.gold + summary.loot.gold).coerceAtMost(cap.gold),
          wood = (player.wood + summary.loot.wood).coerceAtMost(cap.wood),
          stone = (player.stone + summary.loot.stone).coerceAtMost(cap.stone),
          crystal = (player.crystal + summary.loot.crystal).coerceAtMost(cap.crystal),
        )
      if (summary.victory) {
        updatedPlayer = updatedPlayer.copy(battlesWon = updatedPlayer.battlesWon + 1)
      }
      updatedPlayer = addXp(updatedPlayer, summary.xpEarned)

      if (!summary.isDefenseTest && summary.victory) {
        val stages = dao.getStagesOnce().associateBy { it.stageId }.toMutableMap()
        val prev = stages[summary.stageId]
        val bestStars = maxOf(prev?.starsEarned ?: 0, summary.stars)
        stages[summary.stageId] = StageProgressEntity(summary.stageId, bestStars, unlocked = true)
        if (summary.stageId < GameCatalog.battleStages.size) {
          val nextId = summary.stageId + 1
          val next = stages[nextId]
          stages[nextId] = StageProgressEntity(nextId, next?.starsEarned ?: 0, unlocked = true)
        }
        val totalStars = stages.values.sumOf { it.starsEarned }
        updatedPlayer = updatedPlayer.copy(totalStars = totalStars)
        dao.upsertStages(stages.values.toList())
        incrementMission(MissionCategory.WIN_BATTLES, 1)
        advanceTutorialIfAt(TutorialStep.FIRST_BATTLE, TutorialStep.COMPLETED)
      }
      dao.upsertPlayer(updatedPlayer)
      syncAchievements(updatedPlayer)
    }
  }

  suspend fun claimMission(missionId: String): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val missions = dao.getMissionsOnce()
      val target = missions.firstOrNull { it.id == missionId } ?: return ActionFeedback(false, "مأموریت یافت نشد.")
      if (target.claimed) return ActionFeedback(false, "پاداش قبلاً دریافت شده است.")
      if (target.progress < target.target) return ActionFeedback(false, "این مأموریت هنوز تکمیل نشده است.")

      val cap = computeStorageCap(dao.getBuildingsOnce().map { it.toDomain() })
      var updatedPlayer =
        player.copy(
          gold = (player.gold + target.rewardGold).coerceAtMost(cap.gold),
          wood = (player.wood + target.rewardWood).coerceAtMost(cap.wood),
          stone = (player.stone + target.rewardStone).coerceAtMost(cap.stone),
          crystal = (player.crystal + target.rewardCrystal).coerceAtMost(cap.crystal),
        )
      updatedPlayer = addXp(updatedPlayer, target.rewardXp)
      dao.upsertPlayer(updatedPlayer)
      dao.upsertMissions(missions.map { if (it.id == missionId) it.copy(claimed = true) else it })
      syncAchievements(updatedPlayer)
      ActionFeedback(true, "پاداش مأموریت «${target.titleFa}» دریافت شد!")
    }

  suspend fun claimAchievement(achievementId: String): ActionFeedback =
    mutex.withLock {
      val player = dao.getPlayerOnce() ?: return ActionFeedback(false, "پروفایل یافت نشد.")
      val items = dao.getAchievementsOnce()
      val target = items.firstOrNull { it.id == achievementId } ?: return ActionFeedback(false, "دستاورد یافت نشد.")
      if (target.claimed) return ActionFeedback(false, "قبلاً دریافت شده است.")
      if (target.progress < target.target) return ActionFeedback(false, "شرایط این دستاورد هنوز کامل نشده است.")

      val cap = computeStorageCap(dao.getBuildingsOnce().map { it.toDomain() })
      var updatedPlayer =
        player.copy(crystal = (player.crystal + target.rewardCrystal).coerceAtMost(cap.crystal))
      updatedPlayer = addXp(updatedPlayer, target.rewardXp)
      dao.upsertPlayer(updatedPlayer)
      dao.upsertAchievements(items.map { if (it.id == achievementId) it.copy(claimed = true) else it })
      ActionFeedback(true, "دستاورد «${target.titleFa}» باز شد (+${target.rewardCrystal} کریستال)!")
    }

  suspend fun setTutorialStep(step: TutorialStep) {
    prefs.setTutorialStep(step)
  }

  suspend fun setSoundEnabled(enabled: Boolean) = prefs.setSoundEnabled(enabled)

  suspend fun setAnimationsEnabled(enabled: Boolean) = prefs.setAnimationsEnabled(enabled)

  suspend fun setHighContrastMode(enabled: Boolean) = prefs.setHighContrastMode(enabled)

  suspend fun resetAllGameData() {
    mutex.withLock {
      dao.clearPlayer()
      dao.clearBuildings()
      dao.clearTroops()
      dao.clearTechs()
      dao.clearMissions()
      dao.clearAchievements()
      dao.clearStages()
      prefs.resetPreferences()
      seedInitialData()
    }
  }

  private suspend fun advanceTutorialIfAt(expected: TutorialStep, next: TutorialStep) {
    val current = prefs.getCurrent().tutorialStep
    if (current == expected) {
      prefs.setTutorialStep(next)
    }
  }

  private fun findFreeGridCell(occupied: Set<Pair<Int, Int>>): Pair<Int, Int>? {
    val centerOrder = listOf(3, 4, 2, 5, 1, 6, 0, 7)
    for (y in centerOrder) {
      for (x in centerOrder) {
        if ((x to y) !in occupied) return x to y
      }
    }
    return null
  }

  private fun grantXpAndDeduct(player: PlayerEntity, cost: ResourceBundle, xpGain: Int): PlayerEntity {
    val deducted =
      player.copy(
        gold = (player.gold - cost.gold).coerceAtLeast(0),
        wood = (player.wood - cost.wood).coerceAtLeast(0),
        stone = (player.stone - cost.stone).coerceAtLeast(0),
        crystal = (player.crystal - cost.crystal).coerceAtLeast(0),
      )
    return addXp(deducted, xpGain)
  }

  private fun addXp(player: PlayerEntity, xpGain: Int): PlayerEntity {
    var level = player.level
    var xp = player.xp + xpGain
    while (xp >= level * 220) {
      xp -= level * 220
      level += 1
    }
    return player.copy(level = level, xp = xp)
  }

  private suspend fun incrementMission(category: MissionCategory, amount: Int) {
    val missions = dao.getMissionsOnce()
    val updated =
      missions.map { m ->
        if (m.category == category.name && !m.claimed) {
          m.copy(progress = (m.progress + amount).coerceAtMost(m.target))
        } else {
          m
        }
      }
    dao.upsertMissions(updated)
  }

  private suspend fun syncAchievements(player: PlayerEntity) {
    val current = dao.getAchievementsOnce()
    val updated =
      current.map { a ->
        val newProgress =
          when (a.id) {
            "ach_builder" -> player.totalBuildingsBuilt
            "ach_collector" -> player.totalResourcesCollected
            "ach_commander" -> player.totalTroopsTrained
            "ach_conqueror" -> player.battlesWon
            "ach_stars" -> player.totalStars
            "ach_level" -> player.level
            else -> a.progress
          }
        a.copy(progress = newProgress.coerceAtMost(a.target))
      }
    dao.upsertAchievements(updated)
  }

  private fun buildDailyMissions(): List<MissionEntity> =
    listOf(
      MissionEntity(
        id = "daily_collect",
        titleFa = "برداشت طلایی",
        descriptionFa = "حداقل ۱۲۰ واحد منبع از ساختمان‌ها جمع‌آوری کن.",
        category = MissionCategory.COLLECT_RESOURCES.name,
        progress = 0,
        target = 120,
        rewardGold = 120,
        rewardWood = 100,
        rewardStone = 60,
        rewardCrystal = 15,
        rewardXp = 70,
        claimed = false,
      ),
      MissionEntity(
        id = "daily_build",
        titleFa = "معمار قلمرو",
        descriptionFa = "۲ ساختمان جدید بساز یا ارتقا بده.",
        category = MissionCategory.BUILD_OR_UPGRADE.name,
        progress = 0,
        target = 2,
        rewardGold = 160,
        rewardWood = 140,
        rewardStone = 100,
        rewardCrystal = 20,
        rewardXp = 95,
        claimed = false,
      ),
      MissionEntity(
        id = "daily_train",
        titleFa = "آماده‌باش پادگان",
        descriptionFa = "۵ نیروی جدید برای ارتش خود آموزش بده.",
        category = MissionCategory.TRAIN_TROOPS.name,
        progress = 0,
        target = 5,
        rewardGold = 150,
        rewardWood = 120,
        rewardStone = 80,
        rewardCrystal = 20,
        rewardXp = 85,
        claimed = false,
      ),
      MissionEntity(
        id = "daily_battle",
        titleFa = "پیروزی تاکتیکی",
        descriptionFa = "در ۲ نبرد مقابل دشمنان هوش مصنوعی پیروز شو.",
        category = MissionCategory.WIN_BATTLES.name,
        progress = 0,
        target = 2,
        rewardGold = 240,
        rewardWood = 180,
        rewardStone = 140,
        rewardCrystal = 35,
        rewardXp = 130,
        claimed = false,
      ),
    )

  private fun defaultAchievements(): List<AchievementEntity> =
    listOf(
      AchievementEntity("ach_builder", "معمار بزرگ", "۱۰ ساختمان بساز.", 4, 10, 40, 120, false),
      AchievementEntity("ach_collector", "گنج‌دار قلمرو", "جمعاً ۱۰۰۰ واحد منبع برداشت کن.", 0, 1000, 45, 140, false),
      AchievementEntity("ach_commander", "فرمانده ارتش", "۲۵ نیرو آموزش بده.", 5, 25, 50, 150, false),
      AchievementEntity("ach_conqueror", "فاتح سرزمین‌ها", "در ۵ نبرد پیروز شو.", 0, 5, 70, 220, false),
      AchievementEntity("ach_stars", "ستاره‌چین افسانه‌ای", "۱۵ ستاره از نبردها کسب کن.", 0, 15, 90, 260, false),
      AchievementEntity("ach_level", "فرمانروای برتر", "به سطح ۵ بازیکن برس.", 1, 5, 110, 300, false),
    )
}

private fun PlayerEntity.toDomain(): PlayerProfile =
  PlayerProfile(
    kingdomName = kingdomName,
    activeBiome = BiomeType.entries.firstOrNull { it.name == activeBiome } ?: BiomeType.EMERALD_MEADOW,
    level = level,
    xp = xp,
    resources = ResourceBundle(gold, wood, stone, crystal),
    totalStars = totalStars,
    battlesWon = battlesWon,
    totalBuildingsBuilt = totalBuildingsBuilt,
    totalTroopsTrained = totalTroopsTrained,
    totalResourcesCollected = totalResourcesCollected,
  )

private fun PlayerProfile.toEntity(): PlayerEntity =
  PlayerEntity(
    id = 1,
    kingdomName = kingdomName,
    activeBiome = activeBiome.name,
    level = level,
    xp = xp,
    gold = resources.gold,
    wood = resources.wood,
    stone = resources.stone,
    crystal = resources.crystal,
    totalStars = totalStars,
    battlesWon = battlesWon,
    totalBuildingsBuilt = totalBuildingsBuilt,
    totalTroopsTrained = totalTroopsTrained,
    totalResourcesCollected = totalResourcesCollected,
  )

private fun BuildingEntity.toDomain(): PlacedBuilding =
  PlacedBuilding(
    id = id,
    type = BuildingType.entries.firstOrNull { it.name == type } ?: BuildingType.CITIZEN_COTTAGE,
    level = level,
    gridX = gridX,
    gridY = gridY,
    storedGold = storedGold,
    storedWood = storedWood,
    storedStone = storedStone,
    storedCrystal = storedCrystal,
  )

private fun MissionEntity.toDomain(): MissionItem =
  MissionItem(
    id = id,
    titleFa = titleFa,
    descriptionFa = descriptionFa,
    category = MissionCategory.valueOf(category),
    progress = progress,
    target = target,
    reward = ResourceBundle(rewardGold, rewardWood, rewardStone, rewardCrystal),
    rewardXp = rewardXp,
    claimed = claimed,
  )

private fun AchievementEntity.toDomain(): AchievementItem =
  AchievementItem(
    id = id,
    titleFa = titleFa,
    descriptionFa = descriptionFa,
    progress = progress,
    target = target,
    rewardCrystal = rewardCrystal,
    rewardXp = rewardXp,
    claimed = claimed,
  )
