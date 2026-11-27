package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.data.local.GameDatabase
import com.example.data.preferences.GamePreferencesRepository
import com.example.engine.BattleEngine
import com.example.model.BiomeType
import com.example.model.BuildingType
import com.example.model.GameCatalog
import com.example.model.TechNodeState
import com.example.model.TechType
import com.example.model.TroopState
import com.example.model.TroopType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameLogicRobolectricTest {
  private lateinit var db: GameDatabase
  private lateinit var repo: GameRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).allowMainThreadQueries().build()
    repo = GameRepository(db.gameDao(), GamePreferencesRepository(context))
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `repository initializes seeds collects builds moves and trains offline`() = runBlocking {
    repo.ensureInitialized()
    val player = repo.playerFlow.first()
    assertEquals(1, player.level)
    assertEquals(BiomeType.EMERALD_MEADOW, player.activeBiome)

    val collectRes = repo.collectAllResources()
    assertTrue(collectRes.success)

    val buildRes = repo.buildBuilding(BuildingType.CITIZEN_COTTAGE)
    assertTrue(buildRes.success)

    val buildings = repo.buildingsFlow.first()
    val cottage = buildings.last { it.type == BuildingType.CITIZEN_COTTAGE }
    val occupied = buildings.map { it.gridX to it.gridY }.toSet()
    val freeSpot =
      (0 until GameCatalog.GRID_SIZE)
        .flatMap { x -> (0 until GameCatalog.GRID_SIZE).map { y -> x to y } }
        .first { it !in occupied }
    val moveRes = repo.moveBuilding(cottage.id, freeSpot.first, freeSpot.second)
    assertTrue(moveRes.success)

    val trainRes = repo.trainTroop(TroopType.FOOT_SOLDIER, 2)
    assertTrue(trainRes.success)
    val troops = repo.troopsFlow.first()
    assertTrue(troops.first { it.type == TroopType.FOOT_SOLDIER }.count >= 5)
  }

  @Test
  fun `battle engine simulates campaign attack and base defense to completion`() {
    val engine = BattleEngine()
    val stage1 = GameCatalog.battleStages.first()
    var state =
      engine.createCampaignBattle(
        stage1,
        mapOf(TroopType.FOOT_SOLDIER to 6, TroopType.ARCHER to 4, TroopType.ENGINEER to 2),
      )

    val troopStates =
      TroopType.entries.map { TroopState(type = it, level = 2, count = 10) }
    val techs = TechType.entries.map { TechNodeState(type = it, level = 1) }

    repeat(12) { idx ->
      val type = state.remainingToDeploy.keys.firstOrNull() ?: return@repeat
      state = engine.deployUnit(state, type, 1f + (idx % 5), 6.2f, troopStates, techs)
    }

    var steps = 0
    while (!state.isFinished && steps < 1500) {
      state = engine.step(state, 0.05f, techs, totalDeployedCount = 12)
      steps++
    }

    assertTrue(state.isFinished)
    val result = requireNotNull(state.result)
    assertTrue(result.victory)
    assertTrue(result.stars >= 1)

    // Base defense test
    var defenseState =
      engine.createBaseDefenseSimulation(
        playerBuildings = GameCatalog.initialPlayerBuildings(),
        biome = BiomeType.EMERALD_MEADOW,
        techs = techs,
        citadelLevel = 1,
      )
    var dSteps = 0
    while (!defenseState.isFinished && dSteps < 1500) {
      defenseState = engine.step(defenseState, 0.05f, techs, totalDeployedCount = 0)
      dSteps++
    }
    assertTrue(defenseState.isFinished)
    assertNotNull(defenseState.result)
  }
}
