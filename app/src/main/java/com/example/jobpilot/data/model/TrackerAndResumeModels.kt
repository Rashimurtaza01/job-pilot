package com.example.jobpilot.data.model

data class InterviewRound(
    val roundName: String,
    val scheduledDateUtc: String,
    val interviewerName: String = "",
    val interviewerRole: String = "",
    val prepNotes: String = "",
    val isCompleted: Boolean = false
)

data class ApplicationTrackerItem(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val status: ApplicationStatus = ApplicationStatus.SAVED,
    val appliedDateUtc: String? = null,
    val nextActionDateUtc: String? = null,
    val nextActionNote: String = "",
    val notes: String = "",
    val salaryTargetOrOffered: String = "",
    val recruiterContact: String = "",
    val originalJobUrl: String = "",
    val interviewRounds: List<InterviewRound> = emptyList(),
    val lastUpdatedUtc: String = ""
)

data class ResearchRunHistory(
    val id: String,
    val query: String,
    val createdAtUtc: String,
    val resultsCount: Int,
    val verifiedCount: Int,
    val sourcesSummary: String,
    val notes: String = "",
    val filterSummary: String = "",
    val resultJobIds: List<String> = emptyList()
)

data class UserResumeProfile(
    val id: String = "primary_profile",
    val fullName: String = "Research Candidate",
    val email: String = "",
    val targetRole: String = "Software Engineer",
    val experienceYears: Int = 3,
    val skills: List<String> = listOf("Kotlin", "Android", "Jetpack Compose", "Coroutines", "System Design", "Git", "REST APIs"),
    val education: String = "B.S. Computer Science",
    val preferredLocations: List<String> = listOf("Remote", "Worldwide"),
    val minSalaryTargetUsd: Double = 90000.0,
    val rawResumeText: String = ""
)

data class ResumeJobMatchAnalysis(
    val jobId: String,
    val overallMatchScore: Int,
    val matchingSkills: List<String>,
    val missingSkills: List<String>,
    val experienceAlignment: String,
    val educationAlignment: String,
    val suggestedImprovements: List<String>,
    val tailoredCoverLetter: String,
    val interviewPrepQuestions: List<String>
)
