package com.example.jobpilot.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobpilot.ui.screens.AiResearchAgentScreen
import com.example.jobpilot.ui.screens.ApplicationTrackerScreen
import com.example.jobpilot.ui.screens.CompanyIntelligenceScreen
import com.example.jobpilot.ui.screens.DashboardScreen
import com.example.jobpilot.ui.screens.DiscoverJobsScreen
import com.example.jobpilot.ui.screens.JobDetailScreen
import com.example.jobpilot.ui.screens.RecruiterIntelligenceScreen
import com.example.jobpilot.ui.screens.ReportsExportScreen
import com.example.jobpilot.ui.screens.ResearchHistoryScreen
import com.example.jobpilot.ui.screens.SavedOpportunitiesScreen
import com.example.jobpilot.ui.screens.ResumeAssistantScreen
import com.example.jobpilot.ui.screens.SettingsScreen
import com.example.jobpilot.ui.viewmodel.JobPilotViewModel
import kotlinx.coroutines.launch

enum class AppDestination(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    AI_AGENT("AI Research", Icons.Default.AutoAwesome),
    DISCOVER("Discover", Icons.Default.Search),
    RECRUITERS("Recruiters", Icons.Default.People),
    COMPANIES("Companies", Icons.Default.Business),
    SAVED("Saved", Icons.Default.Bookmark),
    HISTORY("Research History", Icons.Default.History),
    REPORTS("Reports & Exports", Icons.Default.Description),
    TRACKER("Application Tracker", Icons.Default.Work),
    RESUME("Resume Matrix", Icons.Default.Assessment),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: JobPilotViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }

    val selectedJob by viewModel.selectedJob.collectAsState()

    // Handle physical back button
    BackHandler(enabled = selectedJob != null || currentDestination != AppDestination.DASHBOARD) {
        if (selectedJob != null) {
            viewModel.clearSelectedJob()
        } else if (currentDestination != AppDestination.DASHBOARD) {
            currentDestination = AppDestination.DASHBOARD
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JP",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "JOBPILOT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Global Research & Recruiter Intel",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(10.dp))

                    AppDestination.values().forEach { dest ->
                        NavigationDrawerItem(
                            icon = { Icon(dest.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            label = { Text(dest.title, fontSize = 13.sp, fontWeight = if (currentDestination == dest) FontWeight.Bold else FontWeight.Normal) },
                            selected = currentDestination == dest,
                            onClick = {
                                currentDestination = dest
                                viewModel.clearSelectedJob()
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedJob != null) "Dossier Detail" else currentDestination.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            modifier = Modifier.testTag("app_drawer_toggle_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Navigation Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                if (selectedJob == null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        val bottomDestinations = listOf(
                            AppDestination.DASHBOARD,
                            AppDestination.AI_AGENT,
                            AppDestination.DISCOVER,
                            AppDestination.TRACKER
                        )

                        bottomDestinations.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    currentDestination = dest
                                    viewModel.clearSelectedJob()
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                    unselectedTextColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (selectedJob != null) {
                    JobDetailScreen(
                        job = selectedJob!!,
                        viewModel = viewModel,
                        onBack = { viewModel.clearSelectedJob() }
                    )
                } else {
                    when (currentDestination) {
                        AppDestination.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToAgent = { currentDestination = AppDestination.AI_AGENT },
                            onNavigateToDiscover = { currentDestination = AppDestination.DISCOVER },
                            onSelectJob = { viewModel.selectJob(it) }
                        )
                        AppDestination.AI_AGENT -> AiResearchAgentScreen(
                            viewModel = viewModel,
                            onSelectJob = { viewModel.selectJob(it) },
                            onNavigateToReports = { currentDestination = AppDestination.REPORTS }
                        )
                        AppDestination.DISCOVER -> DiscoverJobsScreen(
                            viewModel = viewModel,
                            onSelectJob = { viewModel.selectJob(it) }
                        )
                        AppDestination.RECRUITERS -> RecruiterIntelligenceScreen(
                            viewModel = viewModel,
                            onSelectJob = { viewModel.selectJob(it) }
                        )
                        AppDestination.COMPANIES -> CompanyIntelligenceScreen(
                            viewModel = viewModel,
                            onSelectJob = { viewModel.selectJob(it) }
                        )
                        AppDestination.SAVED -> SavedOpportunitiesScreen(
                            viewModel = viewModel,
                            onSelectJob = { viewModel.selectJob(it) },
                            onNavigateToDiscover = { currentDestination = AppDestination.DISCOVER }
                        )
                        AppDestination.HISTORY -> ResearchHistoryScreen(
                            viewModel = viewModel,
                            onRerunQuery = { query ->
                                viewModel.startResearchTask(query)
                                currentDestination = AppDestination.AI_AGENT
                            }
                        )
                        AppDestination.REPORTS -> ReportsExportScreen(viewModel = viewModel)
                        AppDestination.TRACKER -> ApplicationTrackerScreen(viewModel = viewModel)
                        AppDestination.RESUME -> ResumeAssistantScreen(viewModel = viewModel)
                        AppDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
