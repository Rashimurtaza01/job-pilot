package com.example.jobpilot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY publishedAtUtc DESC, discoveredAtUtc DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: String): JobEntity?

    @Query("SELECT * FROM jobs WHERE isSaved = 1 ORDER BY discoveredAtUtc DESC")
    fun getSavedJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id IN (:jobIds)")
    suspend fun getJobsByIds(jobIds: List<String>): List<JobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("UPDATE jobs SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedState(id: String, isSaved: Boolean)

    @Query("UPDATE jobs SET trackerStatus = :status WHERE id = :id")
    suspend fun updateTrackerStatus(id: String, status: String?)

    @Query("DELETE FROM jobs")
    suspend fun clearAll()
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY lastUpdatedUtc DESC")
    fun getAllApplications(): Flow<List<ApplicationTrackerEntity>>

    @Query("SELECT * FROM applications WHERE id = :id LIMIT 1")
    suspend fun getApplicationById(id: String): ApplicationTrackerEntity?

    @Query("SELECT * FROM applications WHERE jobId = :jobId LIMIT 1")
    suspend fun getApplicationByJobId(jobId: String): ApplicationTrackerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: ApplicationTrackerEntity)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteApplication(id: String)
}

@Dao
interface ResearchHistoryDao {
    @Query("SELECT * FROM research_history ORDER BY createdAtUtc DESC")
    fun getAllHistory(): Flow<List<ResearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ResearchHistoryEntity)

    @Query("DELETE FROM research_history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM research_history")
    suspend fun clearHistory()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 'primary_profile' LIMIT 1")
    fun getProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'primary_profile' LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)
}
