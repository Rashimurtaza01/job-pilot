package com.example.jobpilot.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobpilot.agent.AiResearchAgentEngine
import com.example.jobpilot.agent.ResumeAssistantEngine
import com.example.jobpilot.data.local.JobPilotDatabase
import com.example.jobpilot.data.model.ApplicationStatus
import com.example.jobpilot.data.model.ApplicationTrackerItem
import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ResearchFilter
import com.example.jobpilot.data.model.ResearchRunHistory
import com.example.jobpilot.data.model.ResearchTaskState
import com.example.jobpilot.data.model.ResumeJobMatchAnalysis
import com.example.jobpilot.data.model.SeniorityLevel
import com.example.jobpilot.data.model.UserResumeProfile
import com.example.jobpilot.data.model.WorkArrangement
import com.example.jobpilot.data.repository.ApplicationRepository
import com.example.jobpilot.data.repository.JobRepository
import com.example.jobpilot.data.repository.ResearchHistoryRepository
import com.example.jobpilot.data.repository.UserProfileRepository
import com.example.jobpilot.export.CsvReportGenerator
import com.example.jobpilot.export.ExcelXmlReportGenerator
import com.example.jobpilot.export.GeneratedExportFile
import com.example.jobpilot.export.PdfReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class JobPilotViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JobPilotDatabase.getInstance(application)
    val jobRepository = JobRepository(db.jobDao())
    val applicationRepository = ApplicationRepository(db.applicationDao())
    val historyRepository = ResearchHistoryRepository(db.researchHistoryDao())
    val userProfileRepository = UserProfileRepository(db.userProfileDao())

    val aiResearchAgent = AiResearchAgentEngine(jobRepository)
    val resumeAssistant = ResumeAssistantEngine()

    // State Flows
    val allJobs: StateFlow<List<JobRecord>> = jobRepository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedJobs: StateFlow<List<JobRecord>> = jobRepository.savedJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val applications: StateFlow<List<ApplicationTrackerItem>> = applicationRepository.applications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val researchHistory: StateFlow<List<ResearchRunHistory>> = historyRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserResumeProfile> = userProfileRepository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserResumeProfile())

    val taskState: StateFlow<ResearchTaskState> = aiResearchAgent.taskState

    // Active Filters for Discover Screen
    private val _discoverFilter = MutableStateFlow(ResearchFilter())
    val discoverFilter: StateFlow<ResearchFilter> = _discoverFilter.asStateFlow()

    // Export files cache
    private val _generatedReports = MutableStateFlow<List<GeneratedExportFile>>(emptyList())
    val generatedReports: StateFlow<List<GeneratedExportFile>> = _generatedReports.asStateFlow()

    // Selected job for detail view
    private val _selectedJob = MutableStateFlow<JobRecord?>(null)
    val selectedJob: StateFlow<JobRecord?> = _selectedJob.asStateFlow()

    // Resume match analysis for selected job
    private val _currentResumeAnalysis = MutableStateFlow<ResumeJobMatchAnalysis?>(null)
    val currentResumeAnalysis: StateFlow<ResumeJobMatchAnalysis?> = _currentResumeAnalysis.asStateFlow()

    init {
        // Seed initial benchmark jobs if database is fresh
        viewModelScope.launch {
            val initial = jobRepository.executeFederatedSearch(ResearchFilter())
            // Save initial research history item
            if (initial.jobs.isNotEmpty()) {
                val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                historyRepository.recordRun(
                    ResearchRunHistory(
                        id = "run_init",
                        query = "Global Remote Technical Opportunities",
                        createdAtUtc = utcFormat.format(Date()),
                        resultsCount = initial.jobs.size,
                        verifiedCount = initial.verifiedCount,
                        sourcesSummary = initial.sourcesSearched.joinToString(", "),
                        notes = "Initial baseline audit across configured feeds.",
                        filterSummary = "Remote, Worldwide",
                        resultJobIds = initial.jobs.map { it.id }
                    )
                )
            }
        }
    }

    fun startResearchTask(query: String) {
        viewModelScope.launch {
            aiResearchAgent.startResearch(query)
            val state = aiResearchAgent.taskState.value
            if (state.jobResults.isNotEmpty()) {
                val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                historyRepository.recordRun(
                    ResearchRunHistory(
                        id = "run_${System.currentTimeMillis()}",
                        query = query,
                        createdAtUtc = utcFormat.format(Date()),
                        resultsCount = state.jobResults.size,
                        verifiedCount = state.verifiedCount,
                        sourcesSummary = state.sourcesSearched.joinToString(", "),
                        notes = state.transparencyNotes.lastOrNull() ?: "",
                        filterSummary = "Parsed from natural language",
                        resultJobIds = state.jobResults.map { it.id }
                    )
                )
            }
        }
    }

    fun pauseResearch() = aiResearchAgent.pause()
    fun resumeResearch() = aiResearchAgent.resume()
    fun cancelResearch() = aiResearchAgent.cancel()

    fun updateDiscoverFilter(filter: ResearchFilter) {
        _discoverFilter.value = filter
    }

    fun toggleSaveJob(jobId: String, currentSaved: Boolean) {
        viewModelScope.launch {
            jobRepository.toggleSaveJob(jobId, !currentSaved)
        }
    }

    fun selectJob(job: JobRecord) {
        _selectedJob.value = job
        _currentResumeAnalysis.value = resumeAssistant.analyzeResumeForJob(userProfile.value, job)
    }

    fun clearSelectedJob() {
        _selectedJob.value = null
        _currentResumeAnalysis.value = null
    }

    fun updateJobApplicationStatus(job: JobRecord, status: ApplicationStatus) {
        viewModelScope.launch {
            jobRepository.updateTrackerStatus(job.id, status)
            val existing = applicationRepository.getApplicationByJobId(job.id)
            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val nowUtc = utcFormat.format(Date())

            val item = existing?.copy(
                status = status,
                lastUpdatedUtc = nowUtc
            ) ?: ApplicationTrackerItem(
                id = "app_" + UUID.randomUUID().toString().substring(0, 8),
                jobId = job.id,
                jobTitle = job.title,
                companyName = job.companyName,
                status = status,
                appliedDateUtc = if (status == ApplicationStatus.APPLIED) nowUtc else null,
                originalJobUrl = job.originalJobUrl,
                lastUpdatedUtc = nowUtc
            )
            applicationRepository.saveApplication(item)
        }
    }

    fun saveApplicationItem(item: ApplicationTrackerItem) {
        viewModelScope.launch {
            applicationRepository.saveApplication(item)
        }
    }

    fun deleteApplication(id: String) {
        viewModelScope.launch {
            applicationRepository.deleteApplication(id)
        }
    }

    fun saveUserProfile(profile: UserResumeProfile) {
        viewModelScope.launch {
            userProfileRepository.saveProfile(profile)
            _selectedJob.value?.let { currentJob ->
                _currentResumeAnalysis.value = resumeAssistant.analyzeResumeForJob(profile, currentJob)
            }
        }
    }

    fun exportCsv(jobs: List<JobRecord>, query: String) {
        val file = CsvReportGenerator.generateCsv(getApplication(), jobs, query)
        _generatedReports.value = listOf(file) + _generatedReports.value
    }

    fun exportExcel(jobs: List<JobRecord>, taskState: ResearchTaskState) {
        val file = ExcelXmlReportGenerator.generateWorkbook(getApplication(), jobs, taskState)
        _generatedReports.value = listOf(file) + _generatedReports.value
    }

    fun exportPdf(jobs: List<JobRecord>, query: String) {
        val file = PdfReportGenerator.generatePdf(getApplication(), jobs, query)
        _generatedReports.value = listOf(file) + _generatedReports.value
    }

    fun clearAllData() {
        viewModelScope.launch {
            jobRepository.clearAll()
            historyRepository.clearHistory()
        }
    }
}
