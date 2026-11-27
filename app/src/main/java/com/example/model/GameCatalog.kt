package com.example.model

object GameCatalog {
  const val GRID_SIZE = 8

  fun buildingBuildCost(type: BuildingType, existingCount: Int, biome: BiomeType): ResourceBundle {
    val scale = 1f + existingCount * 0.35f
    val base =
      when (type) {
        BuildingType.COMMAND_CITADEL -> ResourceBundle(gold = 0, wood = 0, stone = 0)
        BuildingType.CITIZEN_COTTAGE -> ResourceBundle(gold = 80, wood = 100, stone = 30)
        BuildingType.HERO_BARRACKS -> ResourceBundle(gold = 120, wood = 110, stone = 60)
        BuildingType.GOLD_MINE -> ResourceBundle(gold = 60, wood = 90, stone = 40)
        BuildingType.RESOURCE_MILL -> ResourceBundle(gold = 90, wood = 70, stone = 50)
        BuildingType.CRYSTAL_EXTRACTOR -> ResourceBundle(gold = 160, wood = 120, stone = 100, crystal = 15)
        BuildingType.RESOURCE_VAULT -> ResourceBundle(gold = 100, wood = 100, stone = 90)
        BuildingType.TECH_LABORATORY -> ResourceBundle(gold = 180, wood = 140, stone = 130, crystal = 25)
        BuildingType.DEFENSE_TOWER -> ResourceBundle(gold = 130, wood = 90, stone = 120)
        BuildingType.PROTECTIVE_WALL -> ResourceBundle(gold = 20, wood = 25, stone = 55)
      }
    val raw = base.scaled(scale)
    val woodFactor = if (biome == BiomeType.GOLDEN_DUNES) 1.1f else 1f
    return raw.copy(wood = (raw.wood * woodFactor).toInt())
  }

  fun buildingUpgradeCost(type: BuildingType, currentLevel: Int): ResourceBundle {
    val mult = currentLevel * 1.45f
    val base =
      when (type) {
        BuildingType.COMMAND_CITADEL -> ResourceBundle(gold = 260, wood = 220, stone = 180, crystal = 20)
        BuildingType.CITIZEN_COTTAGE -> ResourceBundle(gold = 90, wood = 110, stone = 50)
        BuildingType.HERO_BARRACKS -> ResourceBundle(gold = 140, wood = 130, stone = 80, crystal = 10)
        BuildingType.GOLD_MINE -> ResourceBundle(gold = 80, wood = 100, stone = 60)
        BuildingType.RESOURCE_MILL -> ResourceBundle(gold = 100, wood = 80, stone = 70)
        BuildingType.CRYSTAL_EXTRACTOR -> ResourceBundle(gold = 150, wood = 120, stone = 110, crystal = 20)
        BuildingType.RESOURCE_VAULT -> ResourceBundle(gold = 120, wood = 110, stone = 120)
        BuildingType.TECH_LABORATORY -> ResourceBundle(gold = 200, wood = 150, stone = 150, crystal = 35)
        BuildingType.DEFENSE_TOWER -> ResourceBundle(gold = 140, wood = 100, stone = 140, crystal = 10)
        BuildingType.PROTECTIVE_WALL -> ResourceBundle(gold = 35, wood = 30, stone = 75)
      }
    return base.scaled(mult)
  }

  fun buildingMaxHp(type: BuildingType, level: Int): Int {
    val base =
      when (type) {
        BuildingType.COMMAND_CITADEL -> 620
        BuildingType.CITIZEN_COTTAGE -> 210
        BuildingType.HERO_BARRACKS -> 280
        BuildingType.GOLD_MINE -> 220
        BuildingType.RESOURCE_MILL -> 230
        BuildingType.CRYSTAL_EXTRACTOR -> 240
        BuildingType.RESOURCE_VAULT -> 340
        BuildingType.TECH_LABORATORY -> 300
        BuildingType.DEFENSE_TOWER -> 360
        BuildingType.PROTECTIVE_WALL -> 420
      }
    return (base * (1f + (level - 1) * 0.35f)).toInt()
  }

  val troops: Map<TroopType, TroopDefinition> =
    mapOf(
      TroopType.FOOT_SOLDIER to
        TroopDefinition(
          type = TroopType.FOOT_SOLDIER,
          baseHp = 130,
          baseAttack = 22,
          speed = 1.65f,
          range = 0.85f,
          housingSpace = 1,
          trainCost = ResourceBundle(gold = 35, wood = 15),
          unlockCitadelLevel = 1,
        ),
      TroopType.ARCHER to
        TroopDefinition(
          type = TroopType.ARCHER,
          baseHp = 85,
          baseAttack = 26,
          speed = 1.85f,
          range = 2.8f,
          housingSpace = 1,
          trainCost = ResourceBundle(gold = 35, wood = 30),
          unlockCitadelLevel = 1,
        ),
      TroopType.ENGINEER to
        TroopDefinition(
          type = TroopType.ENGINEER,
          baseHp = 110,
          baseAttack = 24,
          speed = 1.7f,
          range = 0.9f,
          housingSpace = 2,
          trainCost = ResourceBundle(gold = 45, wood = 40, stone = 25),
          unlockCitadelLevel = 1,
        ),
      TroopType.HEAVY_GUARDIAN to
        TroopDefinition(
          type = TroopType.HEAVY_GUARDIAN,
          baseHp = 380,
          baseAttack = 28,
          speed = 1.05f,
          range = 0.9f,
          housingSpace = 3,
          trainCost = ResourceBundle(gold = 70, stone = 60),
          unlockCitadelLevel = 2,
        ),
      TroopType.SCOUT_RIDER to
        TroopDefinition(
          type = TroopType.SCOUT_RIDER,
          baseHp = 120,
          baseAttack = 25,
          speed = 2.6f,
          range = 0.85f,
          housingSpace = 2,
          trainCost = ResourceBundle(gold = 55, wood = 35),
          unlockCitadelLevel = 2,
        ),
      TroopType.STAR_MAGE to
        TroopDefinition(
          type = TroopType.STAR_MAGE,
          baseHp = 135,
          baseAttack = 44,
          speed = 1.55f,
          range = 2.6f,
          housingSpace = 3,
          trainCost = ResourceBundle(gold = 65, crystal = 22),
          unlockCitadelLevel = 3,
        ),
    )

  fun troopUpgradeCost(type: TroopType, currentLevel: Int): ResourceBundle {
    val def = troops.getValue(type)
    return ResourceBundle(
      gold = def.trainCost.gold * 3 * currentLevel,
      wood = def.trainCost.wood * 2 * currentLevel,
      stone = def.trainCost.stone * 2 * currentLevel,
      crystal = (def.trainCost.crystal + 12) * currentLevel,
    )
  }

  fun techUpgradeCost(type: TechType, currentLevel: Int): ResourceBundle {
    val next = currentLevel + 1
    return when (type) {
      TechType.HARVEST_BOOST -> ResourceBundle(gold = 120 * next, wood = 90 * next, crystal = 15 * next)
      TechType.REINFORCED_WALLS -> ResourceBundle(wood = 100 * next, stone = 130 * next, crystal = 15 * next)
      TechType.SHARP_BLADES -> ResourceBundle(gold = 140 * next, stone = 80 * next, crystal = 20 * next)
      TechType.CRYSTAL_SHIELDS -> ResourceBundle(gold = 110 * next, stone = 100 * next, crystal = 22 * next)
      TechType.SWIFT_MARCH -> ResourceBundle(gold = 100 * next, wood = 100 * next, crystal = 18 * next)
      TechType.TREASURE_LUCK -> ResourceBundle(gold = 130 * next, crystal = 25 * next)
    }
  }

  fun initialPlayerBuildings(): List<PlacedBuilding> =
    listOf(
      PlacedBuilding(id = 1, type = BuildingType.COMMAND_CITADEL, level = 1, gridX = 3, gridY = 3),
      PlacedBuilding(id = 2, type = BuildingType.GOLD_MINE, level = 1, gridX = 1, gridY = 3, storedGold = 65),
      PlacedBuilding(id = 3, type = BuildingType.RESOURCE_MILL, level = 1, gridX = 5, gridY = 2, storedWood = 45, storedStone = 30),
      PlacedBuilding(id = 4, type = BuildingType.HERO_BARRACKS, level = 1, gridX = 3, gridY = 5),
      PlacedBuilding(id = 5, type = BuildingType.DEFENSE_TOWER, level = 1, gridX = 2, gridY = 2),
      PlacedBuilding(id = 6, type = BuildingType.CITIZEN_COTTAGE, level = 1, gridX = 5, gridY = 5),
    )

  val battleStages: List<BattleStage> =
    listOf(
      BattleStage(
        id = 1,
        titleFa = "اردوگاه چوبی دشت سبز",
        descriptionFa = "یک پایگاه آموزشی ساده برای اولین تمرین فرماندهی.",
        biome = BiomeType.EMERALD_MEADOW,
        difficulty = DifficultyLevel.EASY,
        recommendedPower = 60,
        reward = ResourceBundle(gold = 180, wood = 150, stone = 90, crystal = 20),
        rewardXp = 85,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 1, 4, 3),
            EnemyBuildingBlueprint(BuildingType.GOLD_MINE, 1, 2, 2),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_MILL, 1, 5, 2),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 1, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 1, 4, 4),
          ),
      ),
      BattleStage(
        id = 2,
        titleFa = "دژکده تپه‌های زمردین",
        descriptionFa = "برج دیده‌بانی از گنجینه طلا محافظت می‌کند.",
        biome = BiomeType.EMERALD_MEADOW,
        difficulty = DifficultyLevel.EASY,
        recommendedPower = 95,
        reward = ResourceBundle(gold = 240, wood = 190, stone = 130, crystal = 30),
        rewardXp = 110,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 1, 4, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 1, 3, 3),
            EnemyBuildingBlueprint(BuildingType.GOLD_MINE, 2, 5, 3),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_VAULT, 1, 2, 2),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 1, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 1, 4, 4),
          ),
      ),
      BattleStage(
        id = 3,
        titleFa = "پاسگاه یخی قله سفید",
        descriptionFa = "هوای سرد سرعت نیروها را کم می‌کند؛ از کماندار کمک بگیر!",
        biome = BiomeType.FROSTY_PEAKS,
        difficulty = DifficultyLevel.MEDIUM,
        recommendedPower = 135,
        reward = ResourceBundle(gold = 300, wood = 210, stone = 180, crystal = 45),
        rewardXp = 145,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 2, 4, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 1, 2, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 1, 5, 3),
            EnemyBuildingBlueprint(BuildingType.CRYSTAL_EXTRACTOR, 1, 4, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 1, 2, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 1, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 1, 4, 4),
          ),
      ),
      BattleStage(
        id = 4,
        titleFa = "قلعه بلورین یخچال",
        descriptionFa = "دیوارهای یخی و برج‌های دوقلو راه گنج را بسته‌اند.",
        biome = BiomeType.FROSTY_PEAKS,
        difficulty = DifficultyLevel.MEDIUM,
        recommendedPower = 175,
        reward = ResourceBundle(gold = 350, wood = 250, stone = 220, crystal = 60),
        rewardXp = 180,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 2, 3, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 2, 2, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 2, 5, 2),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_VAULT, 2, 4, 1),
            EnemyBuildingBlueprint(BuildingType.CRYSTAL_EXTRACTOR, 2, 6, 3),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 2, 3),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 3, 3),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 4, 3),
          ),
      ),
      BattleStage(
        id = 5,
        titleFa = "کاروانسرای شن‌های طلایی",
        descriptionFa = "پادگانی پر از طلا در دل تپه‌های کویری.",
        biome = BiomeType.GOLDEN_DUNES,
        difficulty = DifficultyLevel.MEDIUM,
        recommendedPower = 215,
        reward = ResourceBundle(gold = 480, wood = 200, stone = 240, crystal = 65),
        rewardXp = 210,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 2, 4, 2),
            EnemyBuildingBlueprint(BuildingType.GOLD_MINE, 3, 1, 2),
            EnemyBuildingBlueprint(BuildingType.GOLD_MINE, 3, 6, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 2, 3, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 2, 5, 3),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 4, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 5, 4),
          ),
      ),
      BattleStage(
        id = 6,
        titleFa = "ارگ سراب آفتاب",
        descriptionFa = "از مهندسان برای شکستن دیوار کویری استفاده کن.",
        biome = BiomeType.GOLDEN_DUNES,
        difficulty = DifficultyLevel.HARD,
        recommendedPower = 265,
        reward = ResourceBundle(gold = 580, wood = 260, stone = 300, crystal = 80),
        rewardXp = 250,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 3, 4, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 3, 2, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 2, 4, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 3, 6, 2),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_VAULT, 3, 3, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 2, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 4, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 2, 5, 4),
          ),
      ),
      BattleStage(
        id = 7,
        titleFa = "پناهگاه درختان کهن",
        descriptionFa = "مسیرهای پرپیچ‌وخم جنگل محل مناسبی برای واحد شناسایی است.",
        biome = BiomeType.MYSTIC_CANOPY,
        difficulty = DifficultyLevel.HARD,
        recommendedPower = 310,
        reward = ResourceBundle(gold = 550, wood = 480, stone = 320, crystal = 95),
        rewardXp = 290,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 3, 3, 2),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_MILL, 3, 1, 3),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_MILL, 3, 6, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 3, 2, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 3, 5, 3),
            EnemyBuildingBlueprint(BuildingType.CITIZEN_COTTAGE, 3, 4, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 3, 2, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 3, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 3, 4, 4),
          ),
      ),
      BattleStage(
        id = 8,
        titleFa = "تاج‌دژ جنگل افسون",
        descriptionFa = "سه برج دفاعی در میان درختان درخشان نگهبانی می‌دهند.",
        biome = BiomeType.MYSTIC_CANOPY,
        difficulty = DifficultyLevel.HARD,
        recommendedPower = 365,
        reward = ResourceBundle(gold = 650, wood = 550, stone = 380, crystal = 115),
        rewardXp = 335,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 4, 4, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 3, 2, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 3, 4, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 3, 6, 2),
            EnemyBuildingBlueprint(BuildingType.TECH_LABORATORY, 3, 3, 1),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_VAULT, 3, 5, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 3, 1, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 3, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 3, 5, 4),
          ),
      ),
      BattleStage(
        id = 9,
        titleFa = "دژ سنگ‌مذاب آتشفشان",
        descriptionFa = "معدن‌های سنگ آتشفشانی زیر آتش محافظت می‌شوند.",
        biome = BiomeType.VOLCANIC_ISLE,
        difficulty = DifficultyLevel.HARD,
        recommendedPower = 430,
        reward = ResourceBundle(gold = 760, wood = 460, stone = 680, crystal = 140),
        rewardXp = 390,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 4, 4, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 4, 2, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 4, 4, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 4, 6, 2),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_MILL, 4, 1, 2),
            EnemyBuildingBlueprint(BuildingType.CRYSTAL_EXTRACTOR, 3, 5, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 2, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 4, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 5, 4),
          ),
      ),
      BattleStage(
        id = 10,
        titleFa = "قلعه اژدهای سنگی",
        descriptionFa = "یک آزمون واقعی برای ترکیب محافظ سنگین و جادوگر ستاره!",
        biome = BiomeType.VOLCANIC_ISLE,
        difficulty = DifficultyLevel.LEGENDARY,
        recommendedPower = 510,
        reward = ResourceBundle(gold = 900, wood = 580, stone = 800, crystal = 175),
        rewardXp = 450,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 5, 4, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 4, 1, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 4, 3, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 4, 5, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 4, 6, 2),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_VAULT, 4, 3, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 2, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 4, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 4, 5, 4),
          ),
      ),
      BattleStage(
        id = 11,
        titleFa = "رصدخانه سحابی بنفش",
        descriptionFa = "پر از کریستال‌های انرژی در قلمرو اسرارآمیز.",
        biome = BiomeType.ARCANE_NEBULA,
        difficulty = DifficultyLevel.LEGENDARY,
        recommendedPower = 590,
        reward = ResourceBundle(gold = 1050, wood = 720, stone = 750, crystal = 250),
        rewardXp = 520,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 5, 4, 2),
            EnemyBuildingBlueprint(BuildingType.CRYSTAL_EXTRACTOR, 5, 2, 1),
            EnemyBuildingBlueprint(BuildingType.CRYSTAL_EXTRACTOR, 5, 6, 1),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 5, 2, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 5, 4, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 5, 5, 2),
            EnemyBuildingBlueprint(BuildingType.TECH_LABORATORY, 5, 4, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 2, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 4, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 5, 4),
          ),
      ),
      BattleStage(
        id = 12,
        titleFa = "تاج کیهانی دژ ستارگان",
        descriptionFa = "نبرد نهایی افسانه‌ای برای فتح کامل تمام شش قلمرو!",
        biome = BiomeType.ARCANE_NEBULA,
        difficulty = DifficultyLevel.LEGENDARY,
        recommendedPower = 700,
        reward = ResourceBundle(gold = 1400, wood = 950, stone = 950, crystal = 350),
        rewardXp = 700,
        enemyBuildings =
          listOf(
            EnemyBuildingBlueprint(BuildingType.COMMAND_CITADEL, 5, 4, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 5, 1, 2),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 5, 3, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 5, 5, 3),
            EnemyBuildingBlueprint(BuildingType.DEFENSE_TOWER, 5, 6, 2),
            EnemyBuildingBlueprint(BuildingType.RESOURCE_VAULT, 5, 2, 1),
            EnemyBuildingBlueprint(BuildingType.CRYSTAL_EXTRACTOR, 5, 5, 1),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 1, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 2, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 3, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 4, 4),
            EnemyBuildingBlueprint(BuildingType.PROTECTIVE_WALL, 5, 5, 4),
          ),
      ),
    )
}
