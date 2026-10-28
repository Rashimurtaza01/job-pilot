package com.example.jobpilot.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.ui.components.JobOpportunityCard
import com.example.jobpilot.ui.components.MetricKpiCard
import com.example.jobpilot.ui.components.SubtleTag
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Graphite
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.PureBlack
import com.example.jobpilot.ui.viewmodel.JobPilotViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: JobPilotViewModel,
    onNavigateToAgent: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onSelectJob: (JobRecord) -> Unit
) {
    val allJobs by viewModel.allJobs.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val savedJobs by viewModel.savedJobs.collectAsState()

    var quickPrompt by remember { mutableStateOf("") }

    val verifiedCount = allJobs.count { it.fieldEvidence.values.any { ev -> ev.verificationState == com.example.jobpilot.data.model.VerificationState.VERIFIED_SOURCE } }
    val recruitersCount = allJobs.count { it.recruiter != null }
    val verifiedRate = if (allJobs.isNotEmpty()) ((verifiedCount.toDouble() / allJobs.size.toDouble()) * 100).toInt() else 95

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Platform Identity Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INTELLIGENCE ENGINE ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Global Job Research",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // AI Agent Fast Launch Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_agent_fast_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI RESEARCH ANALYST",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Instruct the agent in natural language to search verified feeds, corroborate recruiters, and extract referral programmes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = quickPrompt,
                        onValueChange = { quickPrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_prompt_input"),
                        placeholder = {
                            Text(
                                "Find 100 remote software engineering jobs posted in last 24h...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset prompt chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PresetPromptChip("Remote Junior Software in 24h") {
                            quickPrompt = "Find remote software engineering jobs posted in the last 24 hours. Prioritize junior and entry-level positions."
                        }
                        PresetPromptChip("Android Kotlin Verified") {
                            quickPrompt = "Find remote Android Kotlin engineer roles with verified compensation and recruiter details."
                        }
                        PresetPromptChip("With Official Referral Programs") {
                            quickPrompt = "Find tech vacancies worldwide with documented official employee referral programmes."
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val promptToSend = quickPrompt.ifBlank {
                                "Find 100 remote software engineering jobs worldwide posted in the last 24 hours. Prioritize junior and entry-level positions."
                            }
                            viewModel.startResearchTask(promptToSend)
                            onNavigateToAgent()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("launch_research_btn"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXECUTE RESEARCH TASK", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }

        // 4 KPI Metrics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Vacancies Scanned",
                        value = allJobs.size.toString(),
                        subtitle = "Federated across 2 sources",
                        icon = Icons.Default.Work,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDiscover
                    )
                    MetricKpiCard(
                        title = "Verified Field Rate",
                        value = "$verifiedRate%",
                        subtitle = "ATS / Webhook verified",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Recruiters Mapped",
                        value = recruitersCount.toString(),
                        subtitle = "Public profiles linked",
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Active Pipeline",
                        value = applications.size.toString(),
                        subtitle = "Tracked opportunities",
                        icon = Icons.Default.Explore,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Configured Sources Strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CONNECTED DATA SOURCES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SourceStatusRow("RemoteOK Public API", "Live REST Endpoint", "CONNECTED")
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceStatusRow("Global Corporate Intelligence Feed", "ATS Requisitions & Webhooks", "CONNECTED")
                }
            }
        }

        // Section: Top Verified Opportunities
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LATEST VERIFIED REQUISITIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View All (${allJobs.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable(onClick = onNavigateToDiscover)
                )
            }
        }

        items(allJobs.take(4)) { job ->
            JobOpportunityCard(
                job = job,
                onClick = { onSelectJob(job) },
                onToggleSave = { viewModel.toggleSaveJob(job.id, job.isSaved) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PresetPromptChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SourceStatusRow(name: String, description: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0A2E17))
                .border(0.75.dp, Color(0xFF15803D), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(text = status, color = Color(0xFF4ADE80), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
