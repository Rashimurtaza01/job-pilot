package com.example.jobpilot.data.remote

import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ResearchFilter
import com.example.jobpilot.data.model.SourceConnectionStatus

data class SourceSearchResult(
    val providerId: String,
    val providerName: String,
    val isSuccess: Boolean,
    val records: List<JobRecord>,
    val errorMessage: String? = null,
    val rateLimitRemaining: Int? = null,
    val searchDurationMs: Long = 0,
    val provenanceNotes: String = ""
)

data class SourceVerificationResult(
    val isVerified: Boolean,
    val httpStatusCode: Int,
    val isActiveListing: Boolean,
    val lastVerifiedTimestampUtc: String,
    val notes: String
)

interface JobSourceProvider {
    val providerId: String
    val displayName: String
    val description: String
    val status: SourceConnectionStatus
    val isEnabledByDefault: Boolean

    suspend fun searchJobs(filter: ResearchFilter): SourceSearchResult
    suspend fun verifyListing(jobUrl: String): SourceVerificationResult
}
