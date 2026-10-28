package com.example.jobpilot.data.repository

import com.example.jobpilot.data.local.JobDao
import com.example.jobpilot.data.local.JobEntity
import com.example.jobpilot.data.model.ApplicationStatus
import com.example.jobpilot.data.model.CompanyProfile
import com.example.jobpilot.data.model.FieldEvidence
import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.RecruiterInfo
import com.example.jobpilot.data.model.RecruiterRelationship
import com.example.jobpilot.data.model.ReferralStatus
import com.example.jobpilot.data.model.ResearchFilter
import com.example.jobpilot.data.model.SeniorityLevel
import com.example.jobpilot.data.model.VerificationState
import com.example.jobpilot.data.model.WorkArrangement
import com.example.jobpilot.data.remote.FederatedSearchResult
import com.example.jobpilot.data.remote.SourceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class JobRepository(
    private val jobDao: JobDao,
    private val sourceManager: SourceManager = SourceManager()
) {
    val allJobs: Flow<List<JobRecord>> = jobDao.getAllJobs().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val savedJobs: Flow<List<JobRecord>> = jobDao.getSavedJobs().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun getJobById(id: String): JobRecord? {
        return jobDao.getJobById(id)?.toDomainModel()
    }

    suspend fun toggleSaveJob(id: String, isSaved: Boolean) {
        jobDao.updateSavedState(id, isSaved)
    }

    suspend fun updateTrackerStatus(id: String, status: ApplicationStatus?) {
        jobDao.updateTrackerStatus(id, status?.name)
    }

    suspend fun executeFederatedSearch(filter: ResearchFilter): FederatedSearchResult {
        val result = sourceManager.executeFederatedSearch(filter)
        if (result.jobs.isNotEmpty()) {
            jobDao.insertJobs(result.jobs.map { it.toEntity() })
        }
        return result
    }

    suspend fun insertDirectJobs(jobs: List<JobRecord>) {
        jobDao.insertJobs(jobs.map { it.toEntity() })
    }

    suspend fun clearAll() {
        jobDao.clearAll()
    }

    private fun JobEntity.toDomainModel(): JobRecord {
        val reqSkills = parseJsonList(requiredSkillsJson)
        val prefSkills = parseJsonList(preferredSkillsJson)
        val respList = parseJsonList(responsibilitiesJson)
        val benList = parseJsonList(benefitsJson)
        val factors = parseJsonList(matchFactorsJson)

        val recruiter = if (!recruiterName.isNullOrBlank()) {
            RecruiterInfo(
                id = "rec_$id",
                name = recruiterName,
                professionalTitle = recruiterTitle ?: "Recruiter",
                companyName = companyName,
                publicProfileUrl = recruiterProfileUrl ?: "",
                relationship = try {
                    RecruiterRelationship.valueOf(recruiterRelationship ?: "TALENT_ACQUISITION")
                } catch (_: Exception) {
                    RecruiterRelationship.TALENT_ACQUISITION
                },
                verificationState = try {
                    VerificationState.valueOf(recruiterVerificationState ?: "SOURCE_REPORTED")
                } catch (_: Exception) {
                    VerificationState.SOURCE_REPORTED
                }
            )
        } else null

        val company = CompanyProfile(
            id = "comp_$companyName",
            name = companyName,
            officialWebsite = companyWebsite,
            sector = sector,
            industry = industry,
            referralStatus = try {
                ReferralStatus.valueOf(referralStatus)
            } catch (_: Exception) {
                ReferralStatus.UNVERIFIED
            },
            referralProgramUrl = referralProgramUrl
        )

        val fieldEvidenceMap = mutableMapOf<String, FieldEvidence>()
        if (publishedAtUtc != null) {
            fieldEvidenceMap["published_at"] = FieldEvidence(
                fieldName = "Publication Time",
                extractedValue = publishedAtUtc,
                sourceUrl = originalJobUrl,
                retrievalTimestampUtc = discoveredAtUtc,
                verificationState = VerificationState.VERIFIED_SOURCE
            )
        }
        if (isSalaryDisclosed && salaryMin != null) {
            fieldEvidenceMap["salary"] = FieldEvidence(
                fieldName = "Salary Disclosure",
                extractedValue = "${salaryCurrency} ${salaryMin.toInt()} - ${salaryMax?.toInt() ?: ""}",
                sourceUrl = originalJobUrl,
                retrievalTimestampUtc = discoveredAtUtc,
                verificationState = VerificationState.VERIFIED_SOURCE
            )
        }

        return JobRecord(
            id = id,
            providerJobId = providerJobId,
            title = title,
            companyName = companyName,
            companyWebsite = companyWebsite,
            sector = sector,
            industry = industry,
            seniority = try { SeniorityLevel.valueOf(seniority) } catch (_: Exception) { SeniorityLevel.MID },
            employmentType = employmentType,
            workArrangement = try { WorkArrangement.valueOf(workArrangement) } catch (_: Exception) { WorkArrangement.REMOTE },
            country = country,
            city = city,
            locationString = locationString,
            originalJobUrl = originalJobUrl,
            sourceProvider = sourceProvider,
            publishedAtUtc = publishedAtUtc,
            sourceReportedRelativeAge = sourceReportedRelativeAge,
            discoveredAtUtc = discoveredAtUtc,
            lastCheckedAtUtc = lastCheckedAtUtc,
            lastVerifiedAtUtc = lastVerifiedAtUtc,
            applicationDeadline = applicationDeadline,
            description = description,
            responsibilities = respList,
            requiredSkills = reqSkills,
            preferredSkills = prefSkills,
            salaryMin = salaryMin,
            salaryMax = salaryMax,
            salaryCurrency = salaryCurrency,
            salaryPeriod = salaryPeriod,
            isSalaryDisclosed = isSalaryDisclosed,
            benefits = benList,
            applicationUrl = applicationUrl,
            recruiter = recruiter,
            company = company,
            referralProgram = try { ReferralStatus.valueOf(referralStatus) } catch (_: Exception) { ReferralStatus.UNVERIFIED },
            referralProgramUrl = referralProgramUrl,
            fieldEvidence = fieldEvidenceMap,
            matchScore = matchScore,
            matchFactors = factors,
            isSaved = isSaved,
            applicationTrackerStatus = trackerStatus?.let {
                try { ApplicationStatus.valueOf(it) } catch (_: Exception) { null }
            }
        )
    }

    private fun JobRecord.toEntity(): JobEntity {
        return JobEntity(
            id = id,
            providerJobId = providerJobId,
            title = title,
            companyName = companyName,
            companyWebsite = companyWebsite,
            sector = sector,
            industry = industry,
            seniority = seniority.name,
            employmentType = employmentType,
            workArrangement = workArrangement.name,
            country = country,
            city = city,
            locationString = locationString,
            originalJobUrl = originalJobUrl,
            sourceProvider = sourceProvider,
            publishedAtUtc = publishedAtUtc,
            sourceReportedRelativeAge = sourceReportedRelativeAge,
            discoveredAtUtc = discoveredAtUtc,
            lastCheckedAtUtc = lastCheckedAtUtc,
            lastVerifiedAtUtc = lastVerifiedAtUtc,
            applicationDeadline = applicationDeadline,
            description = description,
            responsibilitiesJson = toJsonArray(responsibilities),
            requiredSkillsJson = toJsonArray(requiredSkills),
            preferredSkillsJson = toJsonArray(preferredSkills),
            salaryMin = salaryMin,
            salaryMax = salaryMax,
            salaryCurrency = salaryCurrency,
            salaryPeriod = salaryPeriod,
            isSalaryDisclosed = isSalaryDisclosed,
            benefitsJson = toJsonArray(benefits),
            applicationUrl = applicationUrl,
            recruiterName = recruiter?.name,
            recruiterTitle = recruiter?.professionalTitle,
            recruiterProfileUrl = recruiter?.publicProfileUrl,
            recruiterRelationship = recruiter?.relationship?.name,
            recruiterVerificationState = recruiter?.verificationState?.name,
            referralStatus = referralProgram.name,
            referralProgramUrl = referralProgramUrl,
            matchScore = matchScore,
            matchFactorsJson = toJsonArray(matchFactors),
            isSaved = isSaved,
            trackerStatus = applicationTrackerStatus?.name
        )
    }

    private fun parseJsonList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun toJsonArray(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }
}
