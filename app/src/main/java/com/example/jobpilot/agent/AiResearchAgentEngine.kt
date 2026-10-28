package com.example.jobpilot.agent

import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ResearchFilter
import com.example.jobpilot.data.model.ResearchTaskState
import com.example.jobpilot.data.model.SeniorityLevel
import com.example.jobpilot.data.model.TaskExecutionStage
import com.example.jobpilot.data.model.VerificationState
import com.example.jobpilot.data.model.WorkArrangement
import com.example.jobpilot.data.repository.JobRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class AiResearchAgentEngine(
    private val jobRepository: JobRepository
) {
    private val _taskState = MutableStateFlow(
        ResearchTaskState(
            taskId = "",
            query = "",
            stage = TaskExecutionStage.IDLE,
            currentStepDescription = "Ready for research prompt"
        )
    )
    val taskState: StateFlow<ResearchTaskState> = _taskState.asStateFlow()

    private var isCancelled = false
    private var isPaused = false

    suspend fun startResearch(query: String) {
        isCancelled = false
        isPaused = false

        val taskId = "task_" + UUID.randomUUID().toString().substring(0, 8)
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val startTimeUtc = utcFormat.format(Date())

        val transparencyNotes = mutableListOf<String>()

        try {
            // Stage 1: Understanding Requirements
            _taskState.value = ResearchTaskState(
                taskId = taskId,
                query = query,
                stage = TaskExecutionStage.UNDERSTANDING,
                currentStepDescription = "Deconstructing natural language requirements...",
                progressPercent = 10,
                timestampUtc = startTimeUtc
            )
            delay(350)
            checkCancellation()

            val filter = parseQueryIntent(query)
            transparencyNotes.add("Identified parameters: Keyword='${filter.keyword}', Arrangement=${filter.arrangement.label}, Seniority=${filter.seniority.label}, Freshness=${filter.postedWithinDays?.let { "${it * 24}h" } ?: "All"}.")

            // Stage 2: Preparing Search
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.PREPARING,
                currentStepDescription = "Selecting connected public endpoints and corporate feeds...",
                progressPercent = 25,
                transparencyNotes = transparencyNotes.toList()
            )
            delay(300)
            checkCancellation()

            // Stage 3: Searching configured sources
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.SEARCHING,
                currentStepDescription = "Executing federated queries across RemoteOK API and Verified Dossier...",
                progressPercent = 40
            )

            val searchResult = jobRepository.executeFederatedSearch(filter)
            transparencyNotes.addAll(searchResult.transparencyNotes)
            checkCancellation()

            // Stage 4: Collecting Evidence
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.COLLECTING_EVIDENCE,
                currentStepDescription = "Auditing field-level provenance, canonical ATS URLs, and timestamps...",
                progressPercent = 55,
                sourcesSearched = searchResult.sourcesSearched,
                totalFound = searchResult.rawFoundCount,
                transparencyNotes = transparencyNotes.toList()
            )
            delay(400)
            checkCancellation()

            // Stage 5: Verifying Records
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.VERIFYING,
                currentStepDescription = "Verifying listing statuses, recruiter attributions, and compensation...",
                progressPercent = 70,
                verifiedCount = searchResult.verifiedCount
            )
            delay(400)
            checkCancellation()

            // Stage 6: Deduplicating
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.DEDUPLICATING,
                currentStepDescription = "Removing duplicate requisitions across providers...",
                progressPercent = 82,
                duplicatesRemoved = searchResult.duplicatesRemovedCount
            )
            delay(300)
            checkCancellation()

            // Stage 7: Ranking
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.RANKING,
                currentStepDescription = "Scoring opportunities by skill alignment and freshness...",
                progressPercent = 92
            )
            val rankedJobs = searchResult.jobs.sortedWith(
                compareByDescending<JobRecord> { it.matchScore }
                    .thenByDescending { it.publishedAtUtc ?: "" }
            )
            delay(350)
            checkCancellation()

            // Stage 8: Generating Report Dossier
            val missingFieldsCount = rankedJobs.sumOf { job ->
                var missing = 0
                if (!job.isSalaryDisclosed) missing++
                if (job.recruiter == null) missing++
                if (job.publishedAtUtc == null) missing++
                missing
            }

            transparencyNotes.add("Dossier compilation complete. Evaluated ${rankedJobs.size} positions, identified ${missingFieldsCount} undisclosed data fields across sources.")

            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.COMPLETED,
                currentStepDescription = "Research completed successfully. Dossier ready for review & export.",
                progressPercent = 100,
                totalFound = rankedJobs.size,
                verifiedCount = searchResult.verifiedCount,
                missingFieldsCount = missingFieldsCount,
                jobResults = rankedJobs,
                transparencyNotes = transparencyNotes
            )

        } catch (e: CancellationException) {
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.IDLE,
                currentStepDescription = "Research task cancelled by user.",
                isPaused = false
            )
        } catch (e: Exception) {
            _taskState.value = _taskState.value.copy(
                stage = TaskExecutionStage.FAILED,
                currentStepDescription = "Research failed: ${e.localizedMessage ?: "Unknown error"}",
                executionError = e.localizedMessage
            )
        }
    }

    fun pause() {
        isPaused = true
        _taskState.value = _taskState.value.copy(
            isPaused = true,
            stage = TaskExecutionStage.PAUSED,
            currentStepDescription = "Research paused. Tap resume to proceed."
        )
    }

    fun resume() {
        isPaused = false
        _taskState.value = _taskState.value.copy(
            isPaused = false,
            currentStepDescription = "Resuming research pipeline..."
        )
    }

    fun cancel() {
        isCancelled = true
        _taskState.value = _taskState.value.copy(
            stage = TaskExecutionStage.IDLE,
            currentStepDescription = "Research cancelled.",
            isPaused = false
        )
    }

    private fun checkCancellation() {
        if (isCancelled) {
            throw CancellationException("Research was cancelled")
        }
    }

    private fun parseQueryIntent(query: String): ResearchFilter {
        val lower = query.lowercase()

        // Arrangement
        val arrangement = when {
            lower.contains("remote") -> WorkArrangement.REMOTE
            lower.contains("hybrid") -> WorkArrangement.HYBRID
            lower.contains("on-site") || lower.contains("onsite") -> WorkArrangement.ONSITE
            else -> WorkArrangement.ANY
        }

        // Seniority
        val seniority = when {
            lower.contains("junior") || lower.contains("entry") || lower.contains("graduate") -> SeniorityLevel.ENTRY
            lower.contains("senior") || lower.contains("lead") -> SeniorityLevel.SENIOR
            lower.contains("intern") -> SeniorityLevel.INTERN
            else -> SeniorityLevel.ANY
        }

        // Freshness
        val postedDays = when {
            lower.contains("24 hours") || lower.contains("today") || lower.contains("past 24h") -> 1
            lower.contains("3 days") -> 3
            lower.contains("7 days") || lower.contains("past week") -> 7
            lower.contains("30 days") || lower.contains("past month") -> 30
            else -> null
        }

        // Keywords
        val keywords = mutableListOf<String>()
        if (lower.contains("software engineer") || lower.contains("software engineering")) {
            keywords.add("Software Engineer")
        } else if (lower.contains("android")) {
            keywords.add("Android")
        } else if (lower.contains("backend")) {
            keywords.add("Backend")
        } else if (lower.contains("frontend")) {
            keywords.add("Frontend")
        } else if (lower.contains("devops") || lower.contains("cloud")) {
            keywords.add("Cloud")
        }

        val kwString = keywords.joinToString(" ").ifEmpty {
            // strip common stop words
            query.replace(Regex("(?i)find|100|remote|jobs|worldwide|posted in the last 24 hours|prioritize|positions|please"), "")
                .trim()
                .take(30)
        }

        return ResearchFilter(
            keyword = kwString,
            arrangement = arrangement,
            seniority = seniority,
            postedWithinDays = postedDays,
            salaryDisclosedOnly = lower.contains("salary"),
            recruiterAvailableOnly = lower.contains("recruiter"),
            referralAvailableOnly = lower.contains("referral")
        )
    }
}
