package com.example.jobpilot.data.remote

import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ResearchFilter
import com.example.jobpilot.data.model.SourceConnectionStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SourceManager(
    val providers: List<JobSourceProvider> = listOf(
        BenchmarkVerifiedDatasetProvider(),
        RemoteOkSourceProvider()
    )
) {
    suspend fun executeFederatedSearch(filter: ResearchFilter): FederatedSearchResult = coroutineScope {
        val searchDeferreds = providers.map { provider ->
            async {
                try {
                    provider.searchJobs(filter)
                } catch (e: Exception) {
                    SourceSearchResult(
                        providerId = provider.providerId,
                        providerName = provider.displayName,
                        isSuccess = false,
                        records = emptyList(),
                        errorMessage = e.localizedMessage ?: "Unknown provider error"
                    )
                }
            }
        }

        val results = searchDeferreds.map { it.await() }

        // Deduplication & Normalization
        val rawRecords = mutableListOf<JobRecord>()
        val sourcesSearched = mutableListOf<String>()
        val providerStatusMap = mutableMapOf<String, SourceConnectionStatus>()
        val transparencyNotes = mutableListOf<String>()

        for (res in results) {
            sourcesSearched.add(res.providerName)
            if (res.isSuccess) {
                rawRecords.addAll(res.records)
                transparencyNotes.add("${res.providerName}: Retrieved ${res.records.size} records in ${res.searchDurationMs}ms.")
            } else {
                transparencyNotes.add("${res.providerName}: ${res.errorMessage ?: "Search failed"}")
            }
        }

        // Deduplicate carefully by canonical URL and normalized (Title + Company)
        val deduplicated = mutableListOf<JobRecord>()
        val seenKeys = mutableSetOf<String>()
        var duplicatesRemoved = 0

        for (job in rawRecords) {
            val canonicalKey = job.originalJobUrl.ifBlank {
                "${job.title.lowercase().trim()}::${job.companyName.lowercase().trim()}"
            }
            if (seenKeys.contains(canonicalKey)) {
                duplicatesRemoved++
            } else {
                seenKeys.add(canonicalKey)
                deduplicated.add(job)
            }
        }

        // Calculate verified records count
        val verifiedCount = deduplicated.count { job ->
            job.fieldEvidence.values.any { it.verificationState == com.example.jobpilot.data.model.VerificationState.VERIFIED_SOURCE }
        }

        FederatedSearchResult(
            jobs = deduplicated,
            sourcesSearched = sourcesSearched,
            rawFoundCount = rawRecords.size,
            deduplicatedCount = deduplicated.size,
            duplicatesRemovedCount = duplicatesRemoved,
            verifiedCount = verifiedCount,
            transparencyNotes = transparencyNotes
        )
    }
}

data class FederatedSearchResult(
    val jobs: List<JobRecord>,
    val sourcesSearched: List<String>,
    val rawFoundCount: Int,
    val deduplicatedCount: Int,
    val duplicatesRemovedCount: Int,
    val verifiedCount: Int,
    val transparencyNotes: List<String>
)
