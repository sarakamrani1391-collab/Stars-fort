package com.example.model

data class ResourceBundle(
  val gold: Int = 0,
  val wood: Int = 0,
  val stone: Int = 0,
  val crystal: Int = 0,
) {
  operator fun plus(other: ResourceBundle) =
    ResourceBundle(
      gold = gold + other.gold,
      wood = wood + other.wood,
      stone = stone + other.stone,
      crystal = crystal + other.crystal,
    )

  fun canAfford(cost: ResourceBundle): Boolean =
    gold >= cost.gold && wood >= cost.wood && stone >= cost.stone && crystal >= cost.crystal

  fun scaled(factor: Float): ResourceBundle =
    ResourceBundle(
      gold = (gold * factor).toInt(),
      wood = (wood * factor).toInt(),
      stone = (stone * factor).toInt(),
      crystal = (crystal * factor).toInt(),
    )
}

data class PlayerProfile(
  val kingdomName: String = "دژ آفتاب",
  val activeBiome: BiomeType = BiomeType.EMERALD_MEADOW,
  val level: Int = 1,
  val xp: Int = 0,
  val resources: ResourceBundle = ResourceBundle(gold = 450, wood = 350, stone = 250, crystal = 80),
  val totalStars: Int = 0,
  val battlesWon: Int = 0,
  val totalBuildingsBuilt: Int = 4,
  val totalTroopsTrained: Int = 4,
  val totalResourcesCollected: Int = 0,
) {
  val xpForNextLevel: Int
    get() = level * 220

  val xpProgress: Float
    get() = (xp.toFloat() / xpForNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class PlacedBuilding(
  val id: Long,
  val type: BuildingType,
  val level: Int,
  val gridX: Int,
  val gridY: Int,
  val storedGold: Int = 0,
  val storedWood: Int = 0,
  val storedStone: Int = 0,
  val storedCrystal: Int = 0,
)

data class TroopDefinition(
  val type: TroopType,
  val baseHp: Int,
  val baseAttack: Int,
  val speed: Float,
  val range: Float,
  val housingSpace: Int,
  val trainCost: ResourceBundle,
  val unlockCitadelLevel: Int,
)

data class TroopState(
  val type: TroopType,
  val level: Int,
  val count: Int,
)

data class TechNodeState(
  val type: TechType,
  val level: Int,
)

data class EnemyBuildingBlueprint(
  val type: BuildingType,
  val level: Int,
  val gridX: Int,
  val gridY: Int,
)

data class BattleStage(
  val id: Int,
  val titleFa: String,
  val descriptionFa: String,
  val biome: BiomeType,
  val difficulty: DifficultyLevel,
  val recommendedPower: Int,
  val reward: ResourceBundle,
  val rewardXp: Int,
  val enemyBuildings: List<EnemyBuildingBlueprint>,
  val starsEarned: Int = 0,
  val unlocked: Boolean = false,
)

data class MissionItem(
  val id: String,
  val titleFa: String,
  val descriptionFa: String,
  val category: MissionCategory,
  val progress: Int,
  val target: Int,
  val reward: ResourceBundle,
  val rewardXp: Int,
  val claimed: Boolean,
) {
  val isComplete: Boolean
    get() = progress >= target
}

data class AchievementItem(
  val id: String,
  val titleFa: String,
  val descriptionFa: String,
  val progress: Int,
  val target: Int,
  val rewardCrystal: Int,
  val rewardXp: Int,
  val claimed: Boolean,
) {
  val isComplete: Boolean
    get() = progress >= target
}

data class BattleBuildingRuntime(
  val id: Int,
  val type: BuildingType,
  val level: Int,
  val gridX: Float,
  val gridY: Float,
  val maxHp: Int,
  val hp: Int,
  val attackDamage: Int,
  val attackRange: Float,
  val cooldownRemaining: Float = 0f,
) {
  val isDestroyed: Boolean
    get() = hp <= 0
}

data class BattleUnitRuntime(
  val id: Int,
  val type: TroopType,
  val level: Int,
  val x: Float,
  val y: Float,
  val maxHp: Int,
  val hp: Int,
  val attack: Int,
  val speed: Float,
  val range: Float,
  val cooldownRemaining: Float = 0f,
  val isEnemy: Boolean = false,
) {
  val isAlive: Boolean
    get() = hp > 0
}

data class BattleProjectile(
  val id: Int,
  val fromX: Float,
  val fromY: Float,
  val toX: Float,
  val toY: Float,
  val progress: Float,
  val colorHex: Long,
)

data class BattleEffect(
  val id: Int,
  val x: Float,
  val y: Float,
  val age: Float,
  val isStarBurst: Boolean = false,
)

data class BattleResultSummary(
  val stageId: Int,
  val stageTitleFa: String,
  val isDefenseTest: Boolean,
  val victory: Boolean,
  val stars: Int,
  val destructionPercent: Int,
  val loot: ResourceBundle,
  val xpEarned: Int,
  val survivingTroops: Int,
  val totalDeployedTroops: Int,
)

data class BattleSimulationState(
  val stage: BattleStage? = null,
  val isDefenseTest: Boolean = false,
  val isRunning: Boolean = false,
  val isFinished: Boolean = false,
  val elapsedSeconds: Float = 0f,
  val buildings: List<BattleBuildingRuntime> = emptyList(),
  val units: List<BattleUnitRuntime> = emptyList(),
  val projectiles: List<BattleProjectile> = emptyList(),
  val effects: List<BattleEffect> = emptyList(),
  val remainingToDeploy: Map<TroopType, Int> = emptyMap(),
  val selectedDeployTroop: TroopType? = null,
  val totalInitialHp: Int = 1,
  val destructionPercent: Int = 0,
  val citadelDestroyed: Boolean = false,
  val result: BattleResultSummary? = null,
)
