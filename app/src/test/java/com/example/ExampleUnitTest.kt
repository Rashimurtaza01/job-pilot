package com.example

import com.example.jobpilot.agent.ResumeAssistantEngine
import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ResearchFilter
import com.example.jobpilot.data.model.SeniorityLevel
import com.example.jobpilot.data.model.UserResumeProfile
import com.example.jobpilot.data.model.WorkArrangement
import com.example.jobpilot.data.remote.BenchmarkVerifiedDatasetProvider
import com.example.jobpilot.data.remote.SourceManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFederatedSearchAndDeduplication() = runBlocking {
        val sourceManager = SourceManager(listOf(BenchmarkVerifiedDatasetProvider()))
        val filter = ResearchFilter(arrangement = WorkArrangement.REMOTE)
        val result = sourceManager.executeFederatedSearch(filter)

        assertTrue(result.jobs.isNotEmpty())
        assertTrue(result.verifiedCount > 0)
        assertEquals(result.rawFoundCount, result.deduplicatedCount + result.duplicatesRemovedCount)

        // All returned should be remote
        result.jobs.forEach { job ->
            assertEquals(WorkArrangement.REMOTE, job.workArrangement)
        }
    }

    @Test
    fun testSeniorityFilter() = runBlocking {
        val provider = BenchmarkVerifiedDatasetProvider()
        val entryFilter = ResearchFilter(seniority = SeniorityLevel.ENTRY)
        val result = provider.searchJobs(entryFilter)

        assertTrue(result.records.isNotEmpty())
        result.records.forEach { job ->
            assertEquals(SeniorityLevel.ENTRY, job.seniority)
        }
    }

    @Test
    fun testResumeSkillExtractionAndGapAnalysis() {
        val engine = ResumeAssistantEngine()
        val rawText = "Experienced engineer skilled in Kotlin, Android SDK, Jetpack Compose, Coroutines, and PostgreSQL."
        val extracted = engine.extractSkillsFromText(rawText)

        assertTrue(extracted.contains("Kotlin"))
        assertTrue(extracted.contains("Jetpack Compose"))
        assertTrue(extracted.contains("Coroutines"))

        val dummyJob = JobRecord(
            id = "test_job_1",
            title = "Android Developer",
            companyName = "Acme Corp",
            originalJobUrl = "https://example.com/job",
            sourceProvider = "Direct ATS",
            discoveredAtUtc = "2026-10-27T06:00:00Z",
            lastCheckedAtUtc = "2026-10-27T06:00:00Z",
            description = "Android engineer",
            requiredSkills = listOf("Kotlin", "Android", "Rust"),
            preferredSkills = listOf("Jetpack Compose")
        )

        val profile = UserResumeProfile(
            skills = extracted
        )

        val analysis = engine.analyzeResumeForJob(profile, dummyJob)
        assertTrue(analysis.matchingSkills.contains("Kotlin"))
        assertTrue(analysis.missingSkills.contains("Rust"))
        assertTrue(analysis.tailoredCoverLetter.contains("Acme Corp"))
        assertTrue(analysis.interviewPrepQuestions.isNotEmpty())
    }
}
