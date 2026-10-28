package com.example.jobpilot.data.model

enum class WorkArrangement(val label: String) {
    REMOTE("Remote"),
    HYBRID("Hybrid"),
    ONSITE("On-site"),
    ANY("Any Arrangement")
}

enum class SeniorityLevel(val label: String) {
    ENTRY("Entry / Junior"),
    MID("Mid-Level"),
    SENIOR("Senior"),
    LEAD("Lead / Principal"),
    EXECUTIVE("Executive"),
    INTERN("Internship"),
    ANY("Any Seniority")
}

enum class VerificationState(val label: String, val badgeText: String) {
    VERIFIED_SOURCE("Verified from source", "VERIFIED"),
    CORROBORATED("Corroborated by multiple sources", "CORROBORATED"),
    SOURCE_REPORTED("Source-reported but not independently verified", "REPORTED"),
    AI_INFERRED("AI-inferred from context", "INFERRED"),
    UNAVAILABLE("Not disclosed by source", "NOT DISCLOSED"),
    NOT_CHECKED("Verification pending", "PENDING"),
    CONFLICTING("Conflicting source evidence", "CONFLICT")
}

enum class RecruiterRelationship(val label: String) {
    JOB_AUTHOR("Job Author"),
    ASSIGNED_RECRUITER("Assigned Recruiter"),
    HIRING_MANAGER("Hiring Manager"),
    TALENT_ACQUISITION("Talent Acquisition"),
    COLLEAGUE_RECRUITER("Colleague Recruiter"),
    UNVERIFIED_MATCH("Unverified Relationship")
}

enum class ReferralStatus(val label: String) {
    ACTIVE_DOCUMENTED("Official Program Documented"),
    PUBLIC_EMPLOYEE_OFFER("Employee Referral Offer"),
    INTERNAL_ONLY("Internal Employees Only"),
    NO_PUBLIC_PROGRAM("No Documented Program"),
    UNVERIFIED("Referral Unverified")
}

enum class ApplicationStatus(val label: String) {
    INTERESTED("Interested"),
    SAVED("Saved"),
    APPLIED("Applied"),
    ASSESSMENT("Assessment"),
    INTERVIEW("Interview"),
    OFFER("Offer"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn")
}

enum class TaskExecutionStage(val stepNumber: Int, val title: String) {
    IDLE(0, "Ready for research"),
    UNDERSTANDING(1, "Understanding requirements"),
    PREPARING(2, "Preparing search parameters"),
    SEARCHING(3, "Searching configured sources"),
    COLLECTING_EVIDENCE(4, "Collecting source evidence"),
    VERIFYING(5, "Verifying records"),
    DEDUPLICATING(6, "Deduplicating results"),
    RANKING(7, "Ranking opportunities"),
    GENERATING_REPORT(8, "Generating research dossier"),
    COMPLETED(8, "Research completed"),
    PAUSED(0, "Research paused"),
    FAILED(0, "Research failed")
}

enum class SourceConnectionStatus(val label: String) {
    CONNECTED("Connected"),
    NOT_CONFIGURED("Not Configured"),
    PERMISSION_REQUIRED("Permission Required"),
    RATE_LIMITED("Rate Limited"),
    TEMPORARILY_UNAVAILABLE("Temporarily Unavailable"),
    AUTHENTICATION_EXPIRED("Auth Expired")
}

data class FieldEvidence(
    val fieldName: String,
    val extractedValue: String,
    val sourceUrl: String,
    val retrievalTimestampUtc: String,
    val sourceReportedTimestampUtc: String? = null,
    val verificationState: VerificationState = VerificationState.SOURCE_REPORTED,
    val confidenceExplanation: String = ""
)

data class RecruiterInfo(
    val id: String,
    val name: String,
    val professionalTitle: String,
    val companyName: String,
    val department: String = "Talent Acquisition",
    val publicProfileUrl: String = "",
    val contactMethod: String = "",
    val profileSource: String = "Public Directory",
    val relationship: RecruiterRelationship = RecruiterRelationship.TALENT_ACQUISITION,
    val verificationState: VerificationState = VerificationState.SOURCE_REPORTED,
    val evidenceSnippet: String = "",
    val retrievalTimestampUtc: String = ""
)

data class CompanyProfile(
    val id: String,
    val name: String,
    val officialWebsite: String = "",
    val publicProfileUrl: String = "",
    val industry: String = "Technology",
    val sector: String = "Software",
    val description: String = "",
    val headquarters: String = "",
    val employeeCountRange: String = "",
    val activeVacanciesCount: Int = 1,
    val careersPageUrl: String = "",
    val referralStatus: ReferralStatus = ReferralStatus.UNVERIFIED,
    val referralProgramUrl: String = "",
    val referralInstructions: String = "",
    val lastVerifiedUtc: String = ""
)

data class JobRecord(
    val id: String,
    val providerJobId: String = "",
    val title: String,
    val companyName: String,
    val companyWebsite: String = "",
    val publicCompanyProfileUrl: String = "",
    val sector: String = "Technology",
    val industry: String = "Software Engineering",
    val jobCategory: String = "Engineering",
    val seniority: SeniorityLevel = SeniorityLevel.MID,
    val employmentType: String = "Full-time",
    val workArrangement: WorkArrangement = WorkArrangement.REMOTE,
    val country: String = "Global",
    val city: String = "Remote",
    val locationString: String = "Worldwide (Remote)",
    val originalJobUrl: String,
    val sourceProvider: String,

    // Timelines (UTC ISO-8601 strings)
    val publishedAtUtc: String? = null,
    val sourceReportedRelativeAge: String = "",
    val discoveredAtUtc: String,
    val lastCheckedAtUtc: String,
    val lastVerifiedAtUtc: String? = null,
    val applicationDeadline: String? = null,
    val timestampPrecision: String = "Minute",

    // Description & Skills
    val description: String,
    val responsibilities: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val preferredSkills: List<String> = emptyList(),
    val experienceRequirements: String = "",
    val educationRequirements: String = "",

    // Compensation
    val salaryMin: Double? = null,
    val salaryMax: Double? = null,
    val salaryCurrency: String = "USD",
    val salaryPeriod: String = "Yearly",
    val isSalaryDisclosed: Boolean = false,
    val benefits: List<String> = emptyList(),

    // Application & Verification
    val workAuthRequirements: String = "Worldwide remote eligible",
    val applicationInstructions: String = "Submit resume via official link",
    val applicationUrl: String = "",
    val recruiter: RecruiterInfo? = null,
    val company: CompanyProfile? = null,
    val referralProgram: ReferralStatus = ReferralStatus.UNVERIFIED,
    val referralProgramUrl: String = "",
    val fieldEvidence: Map<String, FieldEvidence> = emptyMap(),
    val matchScore: Int = 85,
    val matchFactors: List<String> = emptyList(),
    val isSaved: Boolean = false,
    val applicationTrackerStatus: ApplicationStatus? = null
)

data class ResearchFilter(
    val keyword: String = "",
    val titleFilter: String = "",
    val location: String = "",
    val sector: String = "",
    val arrangement: WorkArrangement = WorkArrangement.ANY,
    val seniority: SeniorityLevel = SeniorityLevel.ANY,
    val postedWithinDays: Int? = null, // 1 = 24h, 3 = 3 days, 7 = 7 days, 30 = 30 days
    val salaryDisclosedOnly: Boolean = false,
    val recruiterAvailableOnly: Boolean = false,
    val referralAvailableOnly: Boolean = false,
    val verifiedOnly: Boolean = false,
    val minSalary: Double? = null
)

data class ResearchTaskState(
    val taskId: String,
    val query: String,
    val stage: TaskExecutionStage = TaskExecutionStage.IDLE,
    val currentStepDescription: String = "",
    val progressPercent: Int = 0,
    val sourcesSearched: List<String> = emptyList(),
    val totalFound: Int = 0,
    val verifiedCount: Int = 0,
    val duplicatesRemoved: Int = 0,
    val missingFieldsCount: Int = 0,
    val transparencyNotes: List<String> = emptyList(),
    val jobResults: List<JobRecord> = emptyList(),
    val isPaused: Boolean = false,
    val executionError: String? = null,
    val timestampUtc: String = ""
)
