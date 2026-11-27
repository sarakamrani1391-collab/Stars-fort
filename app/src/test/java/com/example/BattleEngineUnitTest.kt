package com.example

import com.example.engine.BattleEngine
import com.example.model.BiomeType
import com.example.model.GameCatalog
import com.example.model.TechNodeState
import com.example.model.TechType
import com.example.model.TroopState
import com.example.model.TroopType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleEngineUnitTest {

  @Test
  fun `all six biomes and twelve stages are configured`() {
    assertEquals(6, BiomeType.entries.size)
    assertEquals(12, GameCatalog.battleStages.size)
    assertEquals(6, TroopType.entries.size)
    assertEquals(6, TechType.entries.size)
  }

  @Test
  fun `campaign battle deploys troops destroys buildings and awards victory`() {
    val engine = BattleEngine()
    val stage1 = GameCatalog.battleStages.first()
    var state =
      engine.createCampaignBattle(
        stage1,
        mapOf(TroopType.FOOT_SOLDIER to 6, TroopType.ARCHER to 4, TroopType.ENGINEER to 2),
      )

    val troopStates = TroopType.entries.map { TroopState(type = it, level = 2, count = 10) }
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
    assertTrue(result.loot.gold > 0)
  }

  @Test
  fun `base defense simulation resolves AI raid against player buildings`() {
    val engine = BattleEngine()
    val techs = TechType.entries.map { TechNodeState(type = it, level = 1) }
    var defenseState =
      engine.createBaseDefenseSimulation(
        playerBuildings = GameCatalog.initialPlayerBuildings(),
        biome = BiomeType.FROSTY_PEAKS,
        techs = techs,
        citadelLevel = 1,
      )
    var steps = 0
    while (!defenseState.isFinished && steps < 1500) {
      defenseState = engine.step(defenseState, 0.05f, techs, totalDeployedCount = 0)
      steps++
    }
    assertTrue(defenseState.isFinished)
    val result = requireNotNull(defenseState.result)
    assertTrue(result.isDefenseTest)
  }
}
