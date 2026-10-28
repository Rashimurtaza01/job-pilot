package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.export.CsvReportGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testReadAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JobPilot", appName)
    }

    @Test
    fun testCsvReportGeneration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleJobs = listOf(
            JobRecord(
                id = "job_test_01",
                title = "Senior Platform Engineer",
                companyName = "Stripe",
                originalJobUrl = "https://stripe.com/jobs/123",
                sourceProvider = "Stripe Careers Direct ATS",
                discoveredAtUtc = "2026-10-27T06:00:00Z",
                lastCheckedAtUtc = "2026-10-27T06:00:00Z",
                description = "Platform infrastructure",
                salaryMin = 160000.0,
                salaryMax = 210000.0,
                isSalaryDisclosed = true
            )
        )

        val report = CsvReportGenerator.generateCsv(context, sampleJobs, "Platform Engineer")
        assertTrue(report.file.exists())
        assertTrue(report.file.length() > 0)
        val content = report.file.readText()
        assertTrue(content.contains("Senior Platform Engineer"))
        assertTrue(content.contains("Stripe"))
        assertTrue(content.contains("160000.0"))
    }
}
