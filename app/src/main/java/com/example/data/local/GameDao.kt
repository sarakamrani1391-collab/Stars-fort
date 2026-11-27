package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
  @Query("SELECT * FROM player_profile WHERE id = 1")
  fun observePlayer(): Flow<PlayerEntity?>

  @Query("SELECT * FROM player_profile WHERE id = 1")
  suspend fun getPlayerOnce(): PlayerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPlayer(player: PlayerEntity)

  @Query("SELECT * FROM buildings ORDER BY id ASC")
  fun observeBuildings(): Flow<List<BuildingEntity>>

  @Query("SELECT * FROM buildings ORDER BY id ASC")
  suspend fun getBuildingsOnce(): List<BuildingEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBuilding(building: BuildingEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBuildings(buildings: List<BuildingEntity>)

  @Update
  suspend fun updateBuilding(building: BuildingEntity)

  @Update
  suspend fun updateBuildings(buildings: List<BuildingEntity>)

  @Query("DELETE FROM buildings")
  suspend fun clearBuildings()

  @Query("SELECT * FROM troops")
  fun observeTroops(): Flow<List<TroopEntity>>

  @Query("SELECT * FROM troops")
  suspend fun getTroopsOnce(): List<TroopEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTroops(troops: List<TroopEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTroop(troop: TroopEntity)

  @Query("SELECT * FROM tech_nodes")
  fun observeTechs(): Flow<List<TechEntity>>

  @Query("SELECT * FROM tech_nodes")
  suspend fun getTechsOnce(): List<TechEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTechs(techs: List<TechEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTech(tech: TechEntity)

  @Query("SELECT * FROM daily_missions")
  fun observeMissions(): Flow<List<MissionEntity>>

  @Query("SELECT * FROM daily_missions")
  suspend fun getMissionsOnce(): List<MissionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertMissions(missions: List<MissionEntity>)

  @Query("DELETE FROM daily_missions")
  suspend fun clearMissions()

  @Query("SELECT * FROM achievements")
  fun observeAchievements(): Flow<List<AchievementEntity>>

  @Query("SELECT * FROM achievements")
  suspend fun getAchievementsOnce(): List<AchievementEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAchievements(achievements: List<AchievementEntity>)

  @Query("SELECT * FROM stage_progress ORDER BY stageId ASC")
  fun observeStages(): Flow<List<StageProgressEntity>>

  @Query("SELECT * FROM stage_progress ORDER BY stageId ASC")
  suspend fun getStagesOnce(): List<StageProgressEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertStages(stages: List<StageProgressEntity>)

  @Query("DELETE FROM player_profile")
  suspend fun clearPlayer()

  @Query("DELETE FROM troops")
  suspend fun clearTroops()

  @Query("DELETE FROM tech_nodes")
  suspend fun clearTechs()

  @Query("DELETE FROM achievements")
  suspend fun clearAchievements()

  @Query("DELETE FROM stage_progress")
  suspend fun clearStages()
}
