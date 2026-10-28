package com.example.jobpilot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ReferralStatus
import com.example.jobpilot.data.model.ResearchFilter
import com.example.jobpilot.data.model.SeniorityLevel
import com.example.jobpilot.data.model.WorkArrangement
import com.example.jobpilot.ui.components.JobOpportunityCard
import com.example.jobpilot.ui.viewmodel.JobPilotViewModel

enum class JobSortOrder(val label: String) {
    MATCH_SCORE("Match Score"),
    POSTING_FRESHNESS("Posting Freshness"),
    SALARY_HIGH("Highest Compensation"),
    COMPANY_NAME("Company Name")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverJobsScreen(
    viewModel: JobPilotViewModel,
    onSelectJob: (JobRecord) -> Unit
) {
    val allJobs by viewModel.allJobs.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }
    var selectedArrangement by remember { mutableStateOf(WorkArrangement.ANY) }
    var selectedSeniority by remember { mutableStateOf(SeniorityLevel.ANY) }
    var selectedDaysPreset by remember { mutableStateOf<Int?>(null) }
    var recruiterOnly by remember { mutableStateOf(false) }
    var referralOnly by remember { mutableStateOf(false) }
    var salaryDisclosedOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(JobSortOrder.MATCH_SCORE) }
    var showFilterDrawer by remember { mutableStateOf(false) }

    // Filter list
    val filteredJobs = allJobs.filter { job ->
        if (searchKeyword.isNotBlank()) {
            val kw = searchKeyword.lowercase()
            val match = job.title.lowercase().contains(kw) ||
                    job.companyName.lowercase().contains(kw) ||
                    job.sector.lowercase().contains(kw) ||
                    job.requiredSkills.any { it.lowercase().contains(kw) } ||
                    job.locationString.lowercase().contains(kw)
            if (!match) return@filter false
        }

        if (selectedArrangement != WorkArrangement.ANY && job.workArrangement != selectedArrangement) {
            return@filter false
        }

        if (selectedSeniority != SeniorityLevel.ANY && job.seniority != selectedSeniority) {
            return@filter false
        }

        if (selectedDaysPreset != null) {
            val age = job.sourceReportedRelativeAge.lowercase()
            val isWithin = when (selectedDaysPreset) {
                1 -> age.contains("hour") || age.contains("minute") || age.contains("today") || age == "1 day ago"
                3 -> age.contains("hour") || age.contains("minute") || age.contains("1 day") || age.contains("2 day") || age.contains("3 day")
                7 -> !age.contains("month") && !age.contains("week")
                else -> true
            }
            if (!isWithin) return@filter false
        }

        if (recruiterOnly && job.recruiter == null) return@filter false
        if (referralOnly && job.referralProgram != ReferralStatus.ACTIVE_DOCUMENTED) return@filter false
        if (salaryDisclosedOnly && !job.isSalaryDisclosed) return@filter false

        true
    }.let { list ->
        when (sortOrder) {
            JobSortOrder.MATCH_SCORE -> list.sortedByDescending { it.matchScore }
            JobSortOrder.POSTING_FRESHNESS -> list.sortedByDescending { it.publishedAtUtc ?: "" }
            JobSortOrder.SALARY_HIGH -> list.sortedByDescending { it.salaryMin ?: 0.0 }
            JobSortOrder.COMPANY_NAME -> list.sortedBy { it.companyName }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Discover Vacancies",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Multi-criteria search engine with ATS verification and evidence trail",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Search Bar & Filter Toggle Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("discover_search_input"),
                    placeholder = { Text("Filter title, skill, or employer...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchKeyword.isNotEmpty()) {
                            IconButton(onClick = { searchKeyword = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (showFilterDrawer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .clickable { showFilterDrawer = !showFilterDrawer }
                        .testTag("toggle_filters_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter Parameters",
                        tint = if (showFilterDrawer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Preset Quick Filter Chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChipToggle("Last 24 Hours", selectedDaysPreset == 1) {
                    selectedDaysPreset = if (selectedDaysPreset == 1) null else 1
                }
                FilterChipToggle("Junior / Entry", selectedSeniority == SeniorityLevel.ENTRY) {
                    selectedSeniority = if (selectedSeniority == SeniorityLevel.ENTRY) SeniorityLevel.ANY else SeniorityLevel.ENTRY
                }
                FilterChipToggle("Remote Only", selectedArrangement == WorkArrangement.REMOTE) {
                    selectedArrangement = if (selectedArrangement == WorkArrangement.REMOTE) WorkArrangement.ANY else WorkArrangement.REMOTE
                }
                FilterChipToggle("Disclosed Salary", salaryDisclosedOnly) {
                    salaryDisclosedOnly = !salaryDisclosedOnly
                }
                FilterChipToggle("Recruiter Linked", recruiterOnly) {
                    recruiterOnly = !recruiterOnly
                }
                FilterChipToggle("Documented Referral", referralOnly) {
                    referralOnly = !referralOnly
                }
            }
        }

        // Expanded Advanced Filters Drawer
        item {
            AnimatedVisibility(visible = showFilterDrawer) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advanced_filters_drawer"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ADVANCED RESEARCH PARAMETERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Work Arrangement", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            WorkArrangement.values().forEach { arr ->
                                FilterChipToggle(arr.label, selectedArrangement == arr) {
                                    selectedArrangement = arr
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Seniority Level", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SeniorityLevel.values().take(5).forEach { sen ->
                                FilterChipToggle(sen.label, selectedSeniority == sen) {
                                    selectedSeniority = sen
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sort Order", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            JobSortOrder.values().forEach { order ->
                                FilterChipToggle(order.label, sortOrder == order) {
                                    sortOrder = order
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    searchKeyword = ""
                                    selectedArrangement = WorkArrangement.ANY
                                    selectedSeniority = SeniorityLevel.ANY
                                    selectedDaysPreset = null
                                    recruiterOnly = false
                                    referralOnly = false
                                    salaryDisclosedOnly = false
                                    sortOrder = JobSortOrder.MATCH_SCORE
                                }
                            ) {
                                Text("Reset All Filters", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Results Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATCHING VACANCIES (${filteredJobs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Sorted by ${sortOrder.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Job List
        if (filteredJobs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching opportunities found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try broadening search terms or resetting filters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        } else {
            items(filteredJobs) { job ->
                JobOpportunityCard(
                    job = job,
                    onClick = { onSelectJob(job) },
                    onToggleSave = { viewModel.toggleSaveJob(job.id, job.isSaved) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FilterChipToggle(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                0.75.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
