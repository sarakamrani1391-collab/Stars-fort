package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities =
    [
      PlayerEntity::class,
      BuildingEntity::class,
      TroopEntity::class,
      TechEntity::class,
      MissionEntity::class,
      AchievementEntity::class,
      StageProgressEntity::class,
    ],
  version = 1,
  exportSchema = false,
)
abstract class GameDatabase : RoomDatabase() {
  abstract fun gameDao(): GameDao

  companion object {
    @Volatile private var INSTANCE: GameDatabase? = null

    fun getInstance(context: Context): GameDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                GameDatabase::class.java,
                "dej_setaregan_db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
