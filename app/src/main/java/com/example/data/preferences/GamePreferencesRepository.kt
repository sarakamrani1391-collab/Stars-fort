package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.TutorialStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "dej_prefs")

data class GamePreferences(
  val soundEffectsEnabled: Boolean = true,
  val animationsEnabled: Boolean = true,
  val highContrastMode: Boolean = false,
  val tutorialStep: TutorialStep = TutorialStep.WELCOME_COLLECT,
  val lastProductionTickMs: Long = 0L,
  val lastMissionDate: String = "",
)

class GamePreferencesRepository(private val context: Context) {
  private object Keys {
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
    val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
    val TUTORIAL_STEP = intPreferencesKey("tutorial_step")
    val LAST_TICK_MS = longPreferencesKey("last_tick_ms")
    val LAST_MISSION_DATE = stringPreferencesKey("last_mission_date")
  }

  val preferences: Flow<GamePreferences> =
    context.gameDataStore.data.map { prefs ->
      val stepIndex = prefs[Keys.TUTORIAL_STEP] ?: 0
      val step = TutorialStep.entries.firstOrNull { it.stepIndex == stepIndex } ?: TutorialStep.COMPLETED
      GamePreferences(
        soundEffectsEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
        animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: true,
        highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
        tutorialStep = step,
        lastProductionTickMs = prefs[Keys.LAST_TICK_MS] ?: 0L,
        lastMissionDate = prefs[Keys.LAST_MISSION_DATE] ?: "",
      )
    }

  suspend fun getCurrent(): GamePreferences = preferences.first()

  suspend fun setSoundEnabled(enabled: Boolean) {
    context.gameDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
  }

  suspend fun setAnimationsEnabled(enabled: Boolean) {
    context.gameDataStore.edit { it[Keys.ANIMATIONS_ENABLED] = enabled }
  }

  suspend fun setHighContrastMode(enabled: Boolean) {
    context.gameDataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
  }

  suspend fun setTutorialStep(step: TutorialStep) {
    context.gameDataStore.edit { it[Keys.TUTORIAL_STEP] = step.stepIndex }
  }

  suspend fun setLastProductionTickMs(timestampMs: Long) {
    context.gameDataStore.edit { it[Keys.LAST_TICK_MS] = timestampMs }
  }

  suspend fun setLastMissionDate(dateKey: String) {
    context.gameDataStore.edit { it[Keys.LAST_MISSION_DATE] = dateKey }
  }

  suspend fun resetPreferences() {
    context.gameDataStore.edit { prefs ->
      prefs[Keys.TUTORIAL_STEP] = 0
      prefs[Keys.LAST_TICK_MS] = System.currentTimeMillis()
      prefs[Keys.LAST_MISSION_DATE] = ""
    }
  }
}
