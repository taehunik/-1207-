package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActiveStopwatchBanner
import com.example.ui.components.AddDDayDialog
import com.example.ui.components.AddSubjectDialog
import com.example.ui.components.AddTodoDialog
import com.example.ui.components.DDayManagerSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.screens.ChartsScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.StudyPlannerTheme
import com.example.util.StudyDate

private data class TabNavItem(
    val tab: StudyTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val studyTabs = listOf(
    TabNavItem(StudyTab.CALENDAR, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    TabNavItem(StudyTab.STOPWATCH, Icons.Filled.Timer, Icons.Outlined.Timer),
    TabNavItem(StudyTab.CHARTS, Icons.Filled.BarChart, Icons.Outlined.BarChart)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlannerApp(
    viewModel: StudyPlannerViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddTodoDialog by remember { mutableStateOf(false) }
    var addTodoForTodayDirectly by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddDDayDialog by remember { mutableStateOf(false) }
    var showDDayManagerSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Back button returns from secondary tabs to CALENDAR (home)
    if (state.currentTab != StudyTab.CALENDAR && !showDDayManagerSheet && !showSettingsSheet) {
        BackHandler {
            viewModel.selectTab(StudyTab.CALENDAR)
        }
    }

    StudyPlannerTheme(darkTheme = state.isDarkMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 700.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = when (state.currentTab) {
                                        StudyTab.CALENDAR -> "스터디노트"
                                        StudyTab.STOPWATCH -> "집중 스톱워치"
                                        StudyTab.CHARTS -> "학습 통계 차트"
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        },
                        actions = {
                            // Explicit in-app Dark Mode Switch right in the top bar for instant toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = if (state.isDarkMode) "다크 모드 켜짐" else "라이트 모드",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Switch(
                                    checked = state.isDarkMode,
                                    onCheckedChange = { viewModel.setDarkMode(it) },
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .testTag("topbar_dark_mode_switch")
                                )
                            }
                            IconButton(
                                onClick = { showDDayManagerSheet = true },
                                modifier = Modifier.testTag("topbar_dday_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = "D-Day 관리"
                                )
                            }
                            IconButton(
                                onClick = { showSettingsSheet = true },
                                modifier = Modifier.testTag("topbar_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "설정 및 데이터 초기화"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            studyTabs.forEach { item ->
                                val selected = state.currentTab == item.tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(item.tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.tab.label
                                        )
                                    },
                                    label = { Text(item.tab.label) },
                                    modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            studyTabs.forEach { item ->
                                val selected = state.currentTab == item.tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(item.tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.tab.label
                                        )
                                    },
                                    label = { Text(item.tab.label) },
                                    modifier = Modifier.testTag("rail_tab_${item.tab.name.lowercase()}")
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 680.dp)
                        ) {
                            // Live persistent stopwatch banner visible across non-Stopwatch tabs
                            if (state.currentTab != StudyTab.STOPWATCH) {
                                val swSubject = state.subjects.firstOrNull {
                                    it.id == state.stopwatch.selectedSubjectId
                                }
                                ActiveStopwatchBanner(
                                    stopwatch = state.stopwatch,
                                    subjectName = swSubject?.name,
                                    subjectColorHex = swSubject?.colorHex,
                                    onTogglePlayPause = {
                                        if (state.stopwatch.isRunning) {
                                            viewModel.pauseStopwatch()
                                        } else {
                                            viewModel.startStopwatch()
                                        }
                                    },
                                    onOpenStopwatch = { viewModel.selectTab(StudyTab.STOPWATCH) }
                                )
                            }

                            Crossfade(
                                targetState = state.currentTab,
                                label = "study_tab_crossfade",
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) { tab ->
                                when (tab) {
                                    StudyTab.CALENDAR -> TodayScreen(
                                        state = state,
                                        onPreviousMonth = viewModel::previousMonth,
                                        onNextMonth = viewModel::nextMonth,
                                        onSelectDate = viewModel::selectDate,
                                        onJumpToToday = viewModel::jumpToToday,
                                        onOpenStopwatchTab = { viewModel.selectTab(StudyTab.STOPWATCH) },
                                        onStartStopwatchDirect = {
                                            viewModel.startStopwatch()
                                            viewModel.selectTab(StudyTab.STOPWATCH)
                                        },
                                        onManageDDays = { showDDayManagerSheet = true },
                                        onAddDDayClick = { showAddDDayDialog = true },
                                        onAddTodoClick = {
                                            addTodoForTodayDirectly = false
                                            showAddTodoDialog = true
                                        },
                                        onToggleTodo = viewModel::toggleTodoCompleted,
                                        onStartTodoStopwatch = viewModel::startStopwatchForTodo,
                                        onDeleteTodo = viewModel::deleteTodo,
                                        onDeleteSession = viewModel::deleteStudySession
                                    )

                                    StudyTab.STOPWATCH -> StopwatchScreen(
                                        state = state,
                                        onSelectSubject = viewModel::selectStopwatchSubject,
                                        onSelectTodo = viewModel::selectStopwatchTodo,
                                        onStart = viewModel::startStopwatch,
                                        onPause = viewModel::pauseStopwatch,
                                        onReset = viewModel::resetStopwatch,
                                        onSaveSession = viewModel::saveStopwatchSession,
                                        onAddSubjectClick = { showAddSubjectDialog = true },
                                        onAddTodoTodayClick = {
                                            addTodoForTodayDirectly = true
                                            showAddTodoDialog = true
                                        },
                                        onDeleteSession = viewModel::deleteStudySession
                                    )

                                    StudyTab.CHARTS -> ChartsScreen(
                                        state = state,
                                        onOpenStopwatch = { viewModel.selectTab(StudyTab.STOPWATCH) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddTodoDialog) {
            val targetDate = if (addTodoForTodayDirectly) StudyDate.today() else state.selectedDate
            AddTodoDialog(
                selectedDate = targetDate,
                subjects = state.subjects,
                onDismiss = { showAddTodoDialog = false },
                onAddTodo = { title, subjectId ->
                    viewModel.addTodo(title, subjectId, targetDate)
                },
                onRequestAddSubject = { showAddSubjectDialog = true }
            )
        }

        if (showAddSubjectDialog) {
            AddSubjectDialog(
                onDismiss = { showAddSubjectDialog = false },
                onAddSubject = viewModel::addSubject
            )
        }

        if (showAddDDayDialog) {
            AddDDayDialog(
                onDismiss = { showAddDDayDialog = false },
                onAddDDay = viewModel::addDDay
            )
        }

        if (showDDayManagerSheet) {
            DDayManagerSheet(
                ddays = state.ddays,
                onDismiss = { showDDayManagerSheet = false },
                onAddNewClick = { showAddDDayDialog = true },
                onSetFeatured = viewModel::setFeaturedDDay,
                onDeleteDDay = viewModel::deleteDDay
            )
        }

        if (showSettingsSheet) {
            SettingsSheet(
                isDarkMode = state.isDarkMode,
                subjects = state.subjects,
                onDismiss = { showSettingsSheet = false },
                onDarkModeChange = viewModel::setDarkMode,
                onAddSubjectClick = { showAddSubjectDialog = true },
                onDeleteSubject = viewModel::deleteSubject,
                onResetAllData = viewModel::resetAllData
            )
        }
    }
}
