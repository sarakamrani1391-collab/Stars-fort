package com.example.engine

import com.example.model.BattleBuildingRuntime
import com.example.model.BattleEffect
import com.example.model.BattleProjectile
import com.example.model.BattleResultSummary
import com.example.model.BattleSimulationState
import com.example.model.BattleStage
import com.example.model.BattleUnitRuntime
import com.example.model.BiomeType
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.model.PlacedBuilding
import com.example.model.ResourceBundle
import com.example.model.TechNodeState
import com.example.model.TechType
import com.example.model.TroopState
import com.example.model.TroopType
import kotlin.math.hypot
import kotlin.math.roundToInt

class BattleEngine {
  private var nextEntityId = 1000

  fun createCampaignBattle(
    stage: BattleStage,
    selectedSquad: Map<TroopType, Int>,
  ): BattleSimulationState {
    val buildings =
      stage.enemyBuildings.mapIndexed { idx, bp ->
        val maxHp = (GameCatalog.buildingMaxHp(bp.type, bp.level) * biomeHpMultiplier(stage.biome)).toInt()
        val isTower = bp.type == BuildingType.DEFENSE_TOWER
        val isCitadel = bp.type == BuildingType.COMMAND_CITADEL
        val dmg =
          when {
            isTower -> ((16 + bp.level * 8) * stage.biome.defenseBonus).toInt()
            isCitadel -> ((8 + bp.level * 4) * stage.biome.defenseBonus).toInt()
            else -> 0
          }
        val range =
          when {
            isTower -> 3.1f
            isCitadel -> 2.1f
            else -> 0f
          }
        BattleBuildingRuntime(
          id = idx + 1,
          type = bp.type,
          level = bp.level,
          gridX = bp.gridX.toFloat(),
          gridY = bp.gridY.toFloat(),
          maxHp = maxHp,
          hp = maxHp,
          attackDamage = dmg,
          attackRange = range,
        )
      }
    val totalHp = buildings.sumOf { it.maxHp }.coerceAtLeast(1)
    val firstAvailable = selectedSquad.entries.firstOrNull { it.value > 0 }?.key
    return BattleSimulationState(
      stage = stage,
      isDefenseTest = false,
      isRunning = true,
      buildings = buildings,
      remainingToDeploy = selectedSquad.filterValues { it > 0 },
      selectedDeployTroop = firstAvailable,
      totalInitialHp = totalHp,
    )
  }

  fun createBaseDefenseSimulation(
    playerBuildings: List<PlacedBuilding>,
    biome: BiomeType,
    techs: List<TechNodeState>,
    citadelLevel: Int,
  ): BattleSimulationState {
    val wallTech = techs.firstOrNull { it.type == TechType.REINFORCED_WALLS }?.level ?: 0
    val hpMult = (1f + wallTech * 0.2f) * biome.defenseBonus
    val buildings =
      playerBuildings.map { pb ->
        val maxHp = (GameCatalog.buildingMaxHp(pb.type, pb.level) * hpMult).toInt()
        val isTower = pb.type == BuildingType.DEFENSE_TOWER
        val isCitadel = pb.type == BuildingType.COMMAND_CITADEL
        val dmg =
          when {
            isTower -> ((20 + pb.level * 9) * biome.defenseBonus).toInt()
            isCitadel -> ((10 + pb.level * 5) * biome.defenseBonus).toInt()
            else -> 0
          }
        BattleBuildingRuntime(
          id = pb.id.toInt(),
          type = pb.type,
          level = pb.level,
          gridX = pb.gridX.toFloat(),
          gridY = pb.gridY.toFloat(),
          maxHp = maxHp,
          hp = maxHp,
          attackDamage = dmg,
          attackRange = if (isTower) 3.3f else if (isCitadel) 2.2f else 0f,
        )
      }

    val raiderCount = 3 + citadelLevel * 2
    val raiders =
      (0 until raiderCount).map { idx ->
        val type =
          when (idx % 4) {
            0 -> TroopType.FOOT_SOLDIER
            1 -> TroopType.ARCHER
            2 -> TroopType.ENGINEER
            else -> TroopType.SCOUT_RIDER
          }
        val def = GameCatalog.troops.getValue(type)
        val scale = 0.85f + citadelLevel * 0.14f
        val hp = (def.baseHp * scale).toInt()
        BattleUnitRuntime(
          id = nextEntityId++,
          type = type,
          level = citadelLevel.coerceAtMost(4),
          x = 0.8f + (idx % 6) * 1.15f,
          y = 7.1f + (idx / 6) * 0.45f,
          maxHp = hp,
          hp = hp,
          attack = (def.baseAttack * scale).toInt(),
          speed = def.speed * biome.troopSpeedMultiplier,
          range = def.range,
          isEnemy = true,
        )
      }

    val dummyStage =
      BattleStage(
        id = 0,
        titleFa = "آزمایش دفاع خودکار پایگاه",
        descriptionFa = "مهاجمان کارتونی به چیدمان فعلی پایگاه شما حمله می‌کنند!",
        biome = biome,
        difficulty = com.example.model.DifficultyLevel.MEDIUM,
        recommendedPower = 100,
        reward = ResourceBundle(gold = 90, wood = 70, stone = 70, crystal = 15),
        rewardXp = 55,
        enemyBuildings = emptyList(),
        unlocked = true,
      )

    return BattleSimulationState(
      stage = dummyStage,
      isDefenseTest = true,
      isRunning = true,
      buildings = buildings,
      units = raiders,
      totalInitialHp = buildings.sumOf { it.maxHp }.coerceAtLeast(1),
    )
  }

  fun deployUnit(
    state: BattleSimulationState,
    troopType: TroopType,
    gridX: Float,
    gridY: Float,
    troopStates: List<TroopState>,
    techs: List<TechNodeState>,
  ): BattleSimulationState {
    if (state.isFinished || state.isDefenseTest) return state
    val remaining = state.remainingToDeploy[troopType] ?: 0
    if (remaining <= 0) return state

    val biome = state.stage?.biome ?: BiomeType.EMERALD_MEADOW
    val troopState = troopStates.firstOrNull { it.type == troopType }
    val level = troopState?.level ?: 1
    val def = GameCatalog.troops.getValue(troopType)
    val sharpLvl = techs.firstOrNull { it.type == TechType.SHARP_BLADES }?.level ?: 0
    val shieldLvl = techs.firstOrNull { it.type == TechType.CRYSTAL_SHIELDS }?.level ?: 0
    val swiftLvl = techs.firstOrNull { it.type == TechType.SWIFT_MARCH }?.level ?: 0

    val levelScale = 1f + (level - 1) * 0.22f
    val hp = (def.baseHp * levelScale * (1f + shieldLvl * 0.12f)).toInt()
    val atk = (def.baseAttack * levelScale * (1f + sharpLvl * 0.12f)).toInt()
    val spd = def.speed * biome.troopSpeedMultiplier * (1f + swiftLvl * 0.10f)

    val clampedX = gridX.coerceIn(0.4f, 7.5f)
    val clampedY = gridY.coerceIn(4.8f, 7.6f)

    val unit =
      BattleUnitRuntime(
        id = nextEntityId++,
        type = troopType,
        level = level,
        x = clampedX,
        y = clampedY,
        maxHp = hp,
        hp = hp,
        attack = atk,
        speed = spd,
        range = def.range,
      )
    val newRemaining =
      state.remainingToDeploy.toMutableMap().apply {
        if (remaining <= 1) remove(troopType) else put(troopType, remaining - 1)
      }
    val nextSelected =
      if (newRemaining.containsKey(troopType)) troopType else newRemaining.keys.firstOrNull()

    return state.copy(
      units = state.units + unit,
      remainingToDeploy = newRemaining,
      selectedDeployTroop = nextSelected,
    )
  }

  fun step(
    state: BattleSimulationState,
    dt: Float,
    techs: List<TechNodeState>,
    totalDeployedCount: Int,
  ): BattleSimulationState {
    if (!state.isRunning || state.isFinished) return state

    val buildings = state.buildings.toMutableList()
    val units = state.units.toMutableList()
    val projectiles =
      state.projectiles
        .map { it.copy(progress = it.progress + dt * 3.4f) }
        .filter { it.progress < 1f }
        .toMutableList()
    val effects =
      state.effects
        .map { it.copy(age = it.age + dt) }
        .filter { it.age < 0.7f }
        .toMutableList()

    // 1. Move and attack for each alive unit
    for (i in units.indices) {
      val unit = units[i]
      if (!unit.isAlive) continue

      val targetIdx = selectTargetBuildingIndex(unit, buildings)
      if (targetIdx == -1) continue

      val target = buildings[targetIdx]
      val dist = hypot(target.gridX - unit.x, target.gridY - unit.y)
      val cooldown = (unit.cooldownRemaining - dt).coerceAtLeast(0f)

      if (dist <= unit.range) {
        if (cooldown <= 0f) {
          val bonusMult =
            when {
              unit.type == TroopType.ENGINEER &&
                (target.type == BuildingType.PROTECTIVE_WALL || target.type == BuildingType.DEFENSE_TOWER) -> 2.0f
              unit.type == TroopType.STAR_MAGE -> 1.25f
              else -> 1.0f
            }
          val dmg = (unit.attack * bonusMult).roundToInt()
          val newHp = (target.hp - dmg).coerceAtLeast(0)
          buildings[targetIdx] = target.copy(hp = newHp)
          if (unit.range > 1.4f) {
            val color = if (unit.type == TroopType.STAR_MAGE) 0xFFA855F7 else 0xFF38BDF8
            projectiles +=
              BattleProjectile(
                id = nextEntityId++,
                fromX = unit.x,
                fromY = unit.y,
                toX = target.gridX,
                toY = target.gridY,
                progress = 0f,
                colorHex = color,
              )
          }
          if (newHp == 0) {
            effects += BattleEffect(nextEntityId++, target.gridX, target.gridY, age = 0f, isStarBurst = true)
          }
          units[i] = unit.copy(cooldownRemaining = 0.85f)
        } else {
          units[i] = unit.copy(cooldownRemaining = cooldown)
        }
      } else {
        val dirX = (target.gridX - unit.x) / dist.coerceAtLeast(0.01f)
        val dirY = (target.gridY - unit.y) / dist.coerceAtLeast(0.01f)
        units[i] =
          unit.copy(
            x = unit.x + dirX * unit.speed * dt,
            y = unit.y + dirY * unit.speed * dt,
            cooldownRemaining = cooldown,
          )
      }
    }

    // 2. Defense towers & Citadel auto-fire at nearest live unit
    for (bIdx in buildings.indices) {
      val b = buildings[bIdx]
      if (b.isDestroyed || b.attackDamage <= 0) continue

      val cd = (b.cooldownRemaining - dt).coerceAtLeast(0f)
      if (cd > 0f) {
        buildings[bIdx] = b.copy(cooldownRemaining = cd)
        continue
      }

      var closestUnitIdx = -1
      var closestDist = Float.MAX_VALUE
      for (uIdx in units.indices) {
        val u = units[uIdx]
        if (!u.isAlive) continue
        val d = hypot(u.x - b.gridX, u.y - b.gridY)
        if (d <= b.attackRange && d < closestDist) {
          closestDist = d
          closestUnitIdx = uIdx
        }
      }
      if (closestUnitIdx != -1) {
        val targetUnit = units[closestUnitIdx]
        val newHp = (targetUnit.hp - b.attackDamage).coerceAtLeast(0)
        units[closestUnitIdx] = targetUnit.copy(hp = newHp)
        projectiles +=
          BattleProjectile(
            id = nextEntityId++,
            fromX = b.gridX,
            fromY = b.gridY,
            toX = targetUnit.x,
            toY = targetUnit.y,
            progress = 0f,
            colorHex = if (b.type == BuildingType.DEFENSE_TOWER) 0xFFF97316 else 0xFFFACC15,
          )
        if (newHp == 0) {
          effects += BattleEffect(nextEntityId++, targetUnit.x, targetUnit.y, age = 0f, isStarBurst = false)
        }
        buildings[bIdx] = b.copy(cooldownRemaining = 0.95f)
      }
    }

    val remainingHp = buildings.sumOf { it.hp.coerceAtLeast(0) }
    val destruction = (((state.totalInitialHp - remainingHp).toFloat() / state.totalInitialHp) * 100f).roundToInt().coerceIn(0, 100)
    val citadelDown = buildings.firstOrNull { it.type == BuildingType.COMMAND_CITADEL }?.isDestroyed == true

    val allBuildingsGone = buildings.all { it.isDestroyed }
    val allUnitsGone = units.none { it.isAlive } && state.remainingToDeploy.isEmpty()
    val timedOut = state.elapsedSeconds + dt >= 65f

    if (allBuildingsGone || allUnitsGone || timedOut) {
      val result =
        buildSummary(
          state = state,
          destruction = destruction,
          citadelDown = citadelDown,
          units = units,
          techs = techs,
          totalDeployedCount = totalDeployedCount,
        )
      return state.copy(
        isRunning = false,
        isFinished = true,
        elapsedSeconds = state.elapsedSeconds + dt,
        buildings = buildings,
        units = units,
        projectiles = projectiles,
        effects = effects,
        destructionPercent = destruction,
        citadelDestroyed = citadelDown,
        result = result,
      )
    }

    return state.copy(
      elapsedSeconds = state.elapsedSeconds + dt,
      buildings = buildings,
      units = units,
      projectiles = projectiles,
      effects = effects,
      destructionPercent = destruction,
      citadelDestroyed = citadelDown,
    )
  }

  private fun selectTargetBuildingIndex(
    unit: BattleUnitRuntime,
    buildings: List<BattleBuildingRuntime>,
  ): Int {
    val aliveIndices = buildings.indices.filter { !buildings[it].isDestroyed }
    if (aliveIndices.isEmpty()) return -1

    val preferred =
      when (unit.type) {
        TroopType.ENGINEER ->
          aliveIndices.filter {
            buildings[it].type == BuildingType.PROTECTIVE_WALL || buildings[it].type == BuildingType.DEFENSE_TOWER
          }
        TroopType.HEAVY_GUARDIAN ->
          aliveIndices.filter { buildings[it].type == BuildingType.DEFENSE_TOWER }
        TroopType.SCOUT_RIDER ->
          aliveIndices.filter {
            buildings[it].type == BuildingType.GOLD_MINE ||
              buildings[it].type == BuildingType.RESOURCE_MILL ||
              buildings[it].type == BuildingType.CRYSTAL_EXTRACTOR ||
              buildings[it].type == BuildingType.RESOURCE_VAULT
          }
        else -> emptyList()
      }

    val pool = preferred.ifEmpty { aliveIndices }
    return pool.minBy { idx ->
      val b = buildings[idx]
      hypot(b.gridX - unit.x, b.gridY - unit.y)
    }
  }

  private fun buildSummary(
    state: BattleSimulationState,
    destruction: Int,
    citadelDown: Boolean,
    units: List<BattleUnitRuntime>,
    techs: List<TechNodeState>,
    totalDeployedCount: Int,
  ): BattleResultSummary {
    val stage = state.stage!!
    if (state.isDefenseTest) {
      val defended = !citadelDown && destruction < 60
      val stars =
        when {
          destruction < 20 -> 3
          destruction < 45 -> 2
          defended -> 1
          else -> 0
        }
      return BattleResultSummary(
        stageId = 0,
        stageTitleFa = stage.titleFa,
        isDefenseTest = true,
        victory = defended,
        stars = stars,
        destructionPercent = destruction,
        loot = if (defended) stage.reward else ResourceBundle(gold = 25, wood = 20),
        xpEarned = if (defended) stage.rewardXp else 20,
        survivingTroops = 0,
        totalDeployedTroops = 0,
      )
    }

    val stars =
      when {
        destruction >= 100 -> 3
        destruction >= 50 && citadelDown -> 2
        destruction >= 50 || citadelDown -> 1
        else -> 0
      }
    val victory = stars > 0
    val luckLvl = techs.firstOrNull { it.type == TechType.TREASURE_LUCK }?.level ?: 0
    val scoutBonus = if (units.any { it.type == TroopType.SCOUT_RIDER }) 1.25f else 1.0f
    val lootMult =
      when (stars) {
        3 -> 1.15f
        2 -> 1.0f
        1 -> 0.7f
        else -> 0.2f
      } * (1f + luckLvl * 0.15f) * scoutBonus

    val loot = stage.reward.scaled(lootMult)
    val xp = (stage.rewardXp * (if (victory) 1f else 0.25f)).toInt()
    val surviving = units.count { it.isAlive }

    return BattleResultSummary(
      stageId = stage.id,
      stageTitleFa = stage.titleFa,
      isDefenseTest = false,
      victory = victory,
      stars = stars,
      destructionPercent = destruction,
      loot = loot,
      xpEarned = xp,
      survivingTroops = surviving,
      totalDeployedTroops = totalDeployedCount,
    )
  }

  private fun biomeHpMultiplier(biome: BiomeType): Float =
    when (biome) {
      BiomeType.EMERALD_MEADOW -> 1.0f
      BiomeType.FROSTY_PEAKS -> 1.12f
      BiomeType.GOLDEN_DUNES -> 1.18f
      BiomeType.MYSTIC_CANOPY -> 1.24f
      BiomeType.VOLCANIC_ISLE -> 1.32f
      BiomeType.ARCANE_NEBULA -> 1.42f
    }
}
