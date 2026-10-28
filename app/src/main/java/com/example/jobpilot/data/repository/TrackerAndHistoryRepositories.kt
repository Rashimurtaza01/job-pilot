package com.example.jobpilot.data.repository

import com.example.jobpilot.data.local.ApplicationDao
import com.example.jobpilot.data.local.ApplicationTrackerEntity
import com.example.jobpilot.data.local.ResearchHistoryDao
import com.example.jobpilot.data.local.ResearchHistoryEntity
import com.example.jobpilot.data.local.UserProfileDao
import com.example.jobpilot.data.local.UserProfileEntity
import com.example.jobpilot.data.model.ApplicationStatus
import com.example.jobpilot.data.model.ApplicationTrackerItem
import com.example.jobpilot.data.model.InterviewRound
import com.example.jobpilot.data.model.ResearchRunHistory
import com.example.jobpilot.data.model.UserResumeProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ApplicationRepository(private val applicationDao: ApplicationDao) {
    val applications: Flow<List<ApplicationTrackerItem>> = applicationDao.getAllApplications().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveApplication(app: ApplicationTrackerItem) {
        applicationDao.insertOrUpdate(app.toEntity())
    }

    suspend fun deleteApplication(id: String) {
        applicationDao.deleteApplication(id)
    }

    suspend fun getApplicationByJobId(jobId: String): ApplicationTrackerItem? {
        return applicationDao.getApplicationByJobId(jobId)?.toDomain()
    }

    private fun ApplicationTrackerEntity.toDomain(): ApplicationTrackerItem {
        val rounds = mutableListOf<InterviewRound>()
        try {
            val arr = JSONArray(interviewRoundsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                rounds.add(
                    InterviewRound(
                        roundName = obj.optString("roundName", "Interview Round"),
                        scheduledDateUtc = obj.optString("scheduledDateUtc", ""),
                        interviewerName = obj.optString("interviewerName", ""),
                        interviewerRole = obj.optString("interviewerRole", ""),
                        prepNotes = obj.optString("prepNotes", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (_: Exception) {}

        return ApplicationTrackerItem(
            id = id,
            jobId = jobId,
            jobTitle = jobTitle,
            companyName = companyName,
            status = try { ApplicationStatus.valueOf(status) } catch (_: Exception) { ApplicationStatus.SAVED },
            appliedDateUtc = appliedDateUtc,
            nextActionDateUtc = nextActionDateUtc,
            nextActionNote = nextActionNote,
            notes = notes,
            salaryTargetOrOffered = salaryTargetOrOffered,
            recruiterContact = recruiterContact,
            originalJobUrl = originalJobUrl,
            interviewRounds = rounds,
            lastUpdatedUtc = lastUpdatedUtc
        )
    }

    private fun ApplicationTrackerItem.toEntity(): ApplicationTrackerEntity {
        val arr = JSONArray()
        interviewRounds.forEach { r ->
            val obj = JSONObject().apply {
                put("roundName", r.roundName)
                put("scheduledDateUtc", r.scheduledDateUtc)
                put("interviewerName", r.interviewerName)
                put("interviewerRole", r.interviewerRole)
                put("prepNotes", r.prepNotes)
                put("isCompleted", r.isCompleted)
            }
            arr.put(obj)
        }

        return ApplicationTrackerEntity(
            id = id,
            jobId = jobId,
            jobTitle = jobTitle,
            companyName = companyName,
            status = status.name,
            appliedDateUtc = appliedDateUtc,
            nextActionDateUtc = nextActionDateUtc,
            nextActionNote = nextActionNote,
            notes = notes,
            salaryTargetOrOffered = salaryTargetOrOffered,
            recruiterContact = recruiterContact,
            originalJobUrl = originalJobUrl,
            interviewRoundsJson = arr.toString(),
            lastUpdatedUtc = lastUpdatedUtc
        )
    }
}

class ResearchHistoryRepository(private val historyDao: ResearchHistoryDao) {
    val history: Flow<List<ResearchRunHistory>> = historyDao.getAllHistory().map { list ->
        list.map { entity ->
            val jobIds = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.resultJobIdsJson)
                for (i in 0 until arr.length()) {
                    jobIds.add(arr.getString(i))
                }
            } catch (_: Exception) {}

            ResearchRunHistory(
                id = entity.id,
                query = entity.query,
                createdAtUtc = entity.createdAtUtc,
                resultsCount = entity.resultsCount,
                verifiedCount = entity.verifiedCount,
                sourcesSummary = entity.sourcesSummary,
                notes = entity.notes,
                filterSummary = entity.filterSummary,
                resultJobIds = jobIds
            )
        }
    }

    suspend fun recordRun(history: ResearchRunHistory) {
        val arr = JSONArray()
        history.resultJobIds.forEach { arr.put(it) }
        historyDao.insertHistory(
            ResearchHistoryEntity(
                id = history.id,
                query = history.query,
                createdAtUtc = history.createdAtUtc,
                resultsCount = history.resultsCount,
                verifiedCount = history.verifiedCount,
                sourcesSummary = history.sourcesSummary,
                notes = history.notes,
                filterSummary = history.filterSummary,
                resultJobIdsJson = arr.toString()
            )
        )
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    suspend fun deleteRun(id: String) {
        historyDao.deleteHistory(id)
    }
}

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    val profile: Flow<UserResumeProfile> = userProfileDao.getProfileFlow().map { entity ->
        if (entity == null) {
            UserResumeProfile()
        } else {
            val skills = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.skillsJson)
                for (i in 0 until arr.length()) skills.add(arr.getString(i))
            } catch (_: Exception) {}

            val locations = mutableListOf<String>()
            try {
                val arr = JSONArray(entity.preferredLocationsJson)
                for (i in 0 until arr.length()) locations.add(arr.getString(i))
            } catch (_: Exception) {}

            UserResumeProfile(
                id = entity.id,
                fullName = entity.fullName,
                email = entity.email,
                targetRole = entity.targetRole,
                experienceYears = entity.experienceYears,
                skills = skills,
                education = entity.education,
                preferredLocations = locations,
                minSalaryTargetUsd = entity.minSalaryTargetUsd,
                rawResumeText = entity.rawResumeText
            )
        }
    }

    suspend fun saveProfile(profile: UserResumeProfile) {
        val skillsArr = JSONArray()
        profile.skills.forEach { skillsArr.put(it) }

        val locsArr = JSONArray()
        profile.preferredLocations.forEach { locsArr.put(it) }

        userProfileDao.saveProfile(
            UserProfileEntity(
                id = profile.id,
                fullName = profile.fullName,
                email = profile.email,
                targetRole = profile.targetRole,
                experienceYears = profile.experienceYears,
                skillsJson = skillsArr.toString(),
                education = profile.education,
                preferredLocationsJson = locsArr.toString(),
                minSalaryTargetUsd = profile.minSalaryTargetUsd,
                rawResumeText = profile.rawResumeText
            )
        )
    }
}
