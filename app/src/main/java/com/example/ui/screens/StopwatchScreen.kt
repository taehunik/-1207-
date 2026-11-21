package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.StudySessionEntity
import com.example.ui.StudyPlannerUiState
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.TimeFormatter

@Composable
fun StopwatchScreen(
    state: StudyPlannerUiState,
    onSelectSubject: (Long?) -> Unit,
    onSelectTodo: (Long?) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSaveSession: (memo: String) -> Unit,
    onAddSubjectClick: () -> Unit,
    onAddTodoTodayClick: () -> Unit,
    onDeleteSession: (StudySessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var memo by remember { mutableStateOf("") }
    val selectedSubject = state.subjects.firstOrNull { it.id == state.stopwatch.selectedSubjectId }
    val accentColor = selectedSubject?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val elapsedMs = state.stopwatch.elapsedMillis
    val secondSweep = ((elapsedMs % 60_000L) / 60_000f) * 360f
    val hasRecordedTime = elapsedMs >= 1000L

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Subject & Todo Linking Card
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("과목 선택", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = onAddSubjectClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("과목 추가")
                        }
                    }

                    if (state.subjects.isEmpty()) {
                        Text(
                            text = "과목이 없으면 '일반 공부'로 기록됩니다. 과목을 추가해 차트와 연동해 보세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.subjects.forEach { subject ->
                                val isSelected = state.stopwatch.selectedSubjectId == subject.id
                                val color = Color(subject.colorHex)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectSubject(subject.id) },
                                    label = { Text(subject.name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    },
                                    modifier = Modifier.testTag("sw_subject_${subject.id}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("할 일 연동 (선택)", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = onAddTodoTodayClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("오늘 할 일 추가")
                        }
                    }

                    val matchingTodos = state.todosForToday
                    if (matchingTodos.isEmpty()) {
                        Text(
                            text = "오늘 등록된 할 일이 없습니다. 과목만 선택해서 바로 공부 시간을 측정할 수 있습니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.stopwatch.selectedTodoId == null,
                                onClick = { onSelectTodo(null) },
                                label = { Text("과목 전체") }
                            )
                            matchingTodos.forEach { todoUi ->
                                val isSelected = state.stopwatch.selectedTodoId == todoUi.todo.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        onSelectTodo(if (isSelected) null else todoUi.todo.id)
                                    },
                                    label = {
                                        Text(
                                            text = todoUi.todo.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Stopwatch Circular Dial & Controls
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = when {
                                state.stopwatch.isRunning -> "${selectedSubject?.name ?: "일반 공부"} · 집중 측정 중"
                                hasRecordedTime -> "${selectedSubject?.name ?: "일반 공부"} · 일시정지"
                                else -> "${selectedSubject?.name ?: "일반 공부"} · 측정 대기"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 12.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = accentColor,
                                startAngle = -90f,
                                sweepAngle = if (elapsedMs > 0L && secondSweep == 0f) 360f else secondSweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = TimeFormatter.formatStopwatchMillis(elapsedMs),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("stopwatch_time_readout")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "다른 탭으로 이동해도 멈추지 않습니다",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Primary Start/Pause & Reset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReset,
                            enabled = elapsedMs > 0L || state.stopwatch.isRunning,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("stopwatch_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("초기화")
                        }

                        Button(
                            onClick = if (state.stopwatch.isRunning) onPause else onStart,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier
                                .weight(1.6f)
                                .minimumInteractiveComponentSize()
                                .testTag("stopwatch_start_pause_button")
                        ) {
                            Icon(
                                imageVector = if (state.stopwatch.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    state.stopwatch.isRunning -> "일시정지"
                                    elapsedMs > 0L -> "계속하기"
                                    else -> "공부 시작"
                                }
                            )
                        }
                    }

                    if (hasRecordedTime) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            label = { Text("한 줄 메모 (선택)") },
                            placeholder = { Text("예: 기출 2단원 복습 완료") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onSaveSession(memo)
                                memo = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("stopwatch_save_sync_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("기록 저장 및 캘린더·차트 자동 반영")
                        }
                    }
                }
            }
        }

        // 3. Today's Saved Sessions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("오늘 측정된 기록", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "총 ${TimeFormatter.formatDurationKorean(state.todayTotalStudySeconds)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (state.todaySessions.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "아직 저장된 오늘의 공부 기록이 없습니다. 스톱워치 측정 후 저장하면 캘린더와 통계 차트에 자동 반영됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.todaySessions, key = { it.session.id }) { sessionUi ->
                val subjColor = sessionUi.subject?.let { Color(it.colorHex) }
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
                                    .background(subjColor)
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
                                color = subjColor
                            )
                            IconButton(onClick = { onDeleteSession(sessionUi.session) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "공부 기록 삭제",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
