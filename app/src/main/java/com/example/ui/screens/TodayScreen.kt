package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.StudySessionEntity
import com.example.data.local.TodoEntity
import com.example.ui.StudyPlannerUiState
import com.example.ui.TodoUiModel
import com.example.ui.components.EmptyStudyStateCard
import com.example.ui.components.FeaturedDDayHeroCard
import com.example.ui.components.TodoItemCard
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.StudyDate
import com.example.util.TimeFormatter

@Composable
fun TodayScreen(
    state: StudyPlannerUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (StudyDate) -> Unit,
    onJumpToToday: () -> Unit,
    onOpenStopwatchTab: () -> Unit,
    onStartStopwatchDirect: () -> Unit,
    onManageDDays: () -> Unit,
    onAddDDayClick: () -> Unit,
    onAddTodoClick: () -> Unit,
    onToggleTodo: (TodoEntity) -> Unit,
    onStartTodoStopwatch: (TodoUiModel) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit,
    onDeleteSession: (StudySessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = StudyDate.today()
    val isTodaySelected = state.selectedDate == today
    val completedCount = state.todosForSelectedDate.count { it.todo.isCompleted }
    val totalCount = state.todosForSelectedDate.size

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Featured D-Day Hero Card
        item {
            FeaturedDDayHeroCard(
                featuredDDay = state.featuredDDay,
                totalDDayCount = state.ddays.size,
                onManageClick = onManageDDays,
                onAddClick = onAddDDayClick
            )
        }

        // 2. Full Monthly Study Calendar (replaces single-row strip)
        item {
            MonthlyStudyCalendarCard(
                calendarYear = state.calendarYear,
                calendarMonth = state.calendarMonth,
                selectedDate = state.selectedDate,
                selectedDateTotalStudySeconds = state.selectedDateTotalStudySeconds,
                calendarDaySummaries = state.calendarDaySummaries,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onJumpToToday = onJumpToToday,
                onSelectDate = onSelectDate
            )
        }

        // 3. Direct Stopwatch Entry Card
        item {
            val activeSubject = state.subjects.firstOrNull { it.id == state.stopwatch.selectedSubjectId }
            val isRunningOrPaused = state.stopwatch.isRunning || state.stopwatch.elapsedMillis > 0L

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = onOpenStopwatchTab)
                    .testTag("today_stopwatch_entry_card"),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "스톱워치",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isRunningOrPaused) {
                                    "${activeSubject?.name ?: "공부"} · ${if (state.stopwatch.isRunning) "측정 중" else "일시정지"}"
                                } else {
                                    "오늘 집중 스톱워치"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isRunningOrPaused) {
                                    TimeFormatter.formatStopwatchMillis(state.stopwatch.elapsedMillis)
                                } else {
                                    "오늘 누적 ${TimeFormatter.formatDurationKorean(state.todayTotalStudySeconds)}"
                                },
                                style = if (isRunningOrPaused) {
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    MaterialTheme.typography.bodySmall
                                },
                                color = if (isRunningOrPaused) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    if (isRunningOrPaused) {
                        FilledTonalButton(
                            onClick = onOpenStopwatchTab,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_open_stopwatch_button")
                        ) {
                            Text("타이머 열기")
                        }
                    } else {
                        Button(
                            onClick = onStartStopwatchDirect,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_start_stopwatch_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("공부 시작")
                        }
                    }
                }
            }
        }

        // 4. Selected Date Study Sessions (when recorded)
        if (state.sessionsForSelectedDate.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTodaySelected) "오늘 스톱워치 기록" else "${state.selectedDate.koreanShortMonthDay()} 스톱워치 기록",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "총 ${TimeFormatter.formatDurationKorean(state.selectedDateTotalStudySeconds)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            items(state.sessionsForSelectedDate, key = { "session_${it.session.id}" }) { sessionUi ->
                val subjectColor = sessionUi.subject?.let { Color(it.colorHex) }
                    ?: MaterialTheme.colorScheme.secondary
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(subjectColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = buildString {
                                        append(sessionUi.subject?.name ?: "일반 공부")
                                        if (!sessionUi.todoTitle.isNullOrBlank()) {
                                            append(" · ${sessionUi.todoTitle}")
                                        }
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (sessionUi.session.memo.isNotBlank()) {
                                    Text(
                                        text = sessionUi.session.memo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TimeFormatter.formatDurationKorean(sessionUi.session.durationSeconds),
                                style = MaterialTheme.typography.labelLarge,
                                color = subjectColor
                            )
                            IconButton(onClick = { onDeleteSession(sessionUi.session) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "기록 삭제",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Selected Date's Todos Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isTodaySelected) "오늘의 할 일" else "${state.selectedDate.koreanFullLabel()} 할 일",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (totalCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "$completedCount / $totalCount",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                FilledTonalButton(
                    onClick = onAddTodoClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("today_add_todo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("할 일 추가")
                }
            }
        }

        // 6. Todo Items or Engaging Empty State
        if (state.todosForSelectedDate.isEmpty()) {
            item {
                EmptyStudyStateCard(
                    title = "등록된 일정이 없습니다",
                    subtitle = "${state.selectedDate.koreanFullLabel()}에 공부할 목표를 추가하고 스톱워치로 집중 시간을 기록해 보세요",
                    buttonLabel = "할 일 추가하기",
                    onActionClick = onAddTodoClick
                )
            }
        } else {
            items(state.todosForSelectedDate, key = { it.todo.id }) { item ->
                TodoItemCard(
                    item = item,
                    onToggleComplete = { onToggleTodo(item.todo) },
                    onStartStopwatch = { onStartTodoStopwatch(item) },
                    onDelete = { onDeleteTodo(item.todo) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
