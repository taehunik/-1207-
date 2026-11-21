package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SubjectEntity::class,
        TodoEntity::class,
        StudySessionEntity::class,
        DDayEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyPlannerDatabase : RoomDatabase() {
    abstract fun studyPlannerDao(): StudyPlannerDao

    companion object {
        @Volatile
        private var INSTANCE: StudyPlannerDatabase? = null

        fun getInstance(context: Context): StudyPlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyPlannerDatabase::class.java,
                    "study_planner.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
