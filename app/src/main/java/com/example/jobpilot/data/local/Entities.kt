package com.example.jobpilot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val providerJobId: String,
    val title: String,
    val companyName: String,
    val companyWebsite: String,
    val sector: String,
    val industry: String,
    val seniority: String,
    val employmentType: String,
    val workArrangement: String,
    val country: String,
    val city: String,
    val locationString: String,
    val originalJobUrl: String,
    val sourceProvider: String,
    val publishedAtUtc: String?,
    val sourceReportedRelativeAge: String,
    val discoveredAtUtc: String,
    val lastCheckedAtUtc: String,
    val lastVerifiedAtUtc: String?,
    val applicationDeadline: String?,
    val description: String,
    val responsibilitiesJson: String,
    val requiredSkillsJson: String,
    val preferredSkillsJson: String,
    val salaryMin: Double?,
    val salaryMax: Double?,
    val salaryCurrency: String,
    val salaryPeriod: String,
    val isSalaryDisclosed: Boolean,
    val benefitsJson: String,
    val applicationUrl: String,
    val recruiterName: String?,
    val recruiterTitle: String?,
    val recruiterProfileUrl: String?,
    val recruiterRelationship: String?,
    val recruiterVerificationState: String?,
    val referralStatus: String,
    val referralProgramUrl: String,
    val matchScore: Int,
    val matchFactorsJson: String,
    val isSaved: Boolean,
    val trackerStatus: String?
)

@Entity(tableName = "applications")
data class ApplicationTrackerEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val status: String,
    val appliedDateUtc: String?,
    val nextActionDateUtc: String?,
    val nextActionNote: String,
    val notes: String,
    val salaryTargetOrOffered: String,
    val recruiterContact: String,
    val originalJobUrl: String,
    val interviewRoundsJson: String,
    val lastUpdatedUtc: String
)

@Entity(tableName = "research_history")
data class ResearchHistoryEntity(
    @PrimaryKey val id: String,
    val query: String,
    val createdAtUtc: String,
    val resultsCount: Int,
    val verifiedCount: Int,
    val sourcesSummary: String,
    val notes: String,
    val filterSummary: String,
    val resultJobIdsJson: String
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "primary_profile",
    val fullName: String,
    val email: String,
    val targetRole: String,
    val experienceYears: Int,
    val skillsJson: String,
    val education: String,
    val preferredLocationsJson: String,
    val minSalaryTargetUsd: Double,
    val rawResumeText: String
)
