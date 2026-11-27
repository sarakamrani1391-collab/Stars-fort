package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerEntity(
  @PrimaryKey val id: Int = 1,
  val kingdomName: String,
  val activeBiome: String,
  val level: Int,
  val xp: Int,
  val gold: Int,
  val wood: Int,
  val stone: Int,
  val crystal: Int,
  val totalStars: Int,
  val battlesWon: Int,
  val totalBuildingsBuilt: Int,
  val totalTroopsTrained: Int,
  val totalResourcesCollected: Int,
)

@Entity(tableName = "buildings")
data class BuildingEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val type: String,
  val level: Int,
  val gridX: Int,
  val gridY: Int,
  val storedGold: Int = 0,
  val storedWood: Int = 0,
  val storedStone: Int = 0,
  val storedCrystal: Int = 0,
)

@Entity(tableName = "troops")
data class TroopEntity(
  @PrimaryKey val type: String,
  val level: Int,
  val count: Int,
)

@Entity(tableName = "tech_nodes")
data class TechEntity(
  @PrimaryKey val type: String,
  val level: Int,
)

@Entity(tableName = "daily_missions")
data class MissionEntity(
  @PrimaryKey val id: String,
  val titleFa: String,
  val descriptionFa: String,
  val category: String,
  val progress: Int,
  val target: Int,
  val rewardGold: Int,
  val rewardWood: Int,
  val rewardStone: Int,
  val rewardCrystal: Int,
  val rewardXp: Int,
  val claimed: Boolean,
)

@Entity(tableName = "achievements")
data class AchievementEntity(
  @PrimaryKey val id: String,
  val titleFa: String,
  val descriptionFa: String,
  val progress: Int,
  val target: Int,
  val rewardCrystal: Int,
  val rewardXp: Int,
  val claimed: Boolean,
)

@Entity(tableName = "stage_progress")
data class StageProgressEntity(
  @PrimaryKey val stageId: Int,
  val starsEarned: Int,
  val unlocked: Boolean,
)
