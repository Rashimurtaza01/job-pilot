package com.example.jobpilot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        JobEntity::class,
        ApplicationTrackerEntity::class,
        ResearchHistoryEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(JobConverters::class)
abstract class JobPilotDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun researchHistoryDao(): ResearchHistoryDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: JobPilotDatabase? = null

        fun getInstance(context: Context): JobPilotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JobPilotDatabase::class.java,
                    "jobpilot_intelligence.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
