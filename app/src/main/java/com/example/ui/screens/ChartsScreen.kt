package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.DailyChartBar
import com.example.ui.StudyPlannerUiState
import com.example.ui.SubjectProgressUi
import com.example.ui.components.EmptyStudyStateCard
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.StudyDate
import com.example.util.TimeFormatter

@Composable
fun ChartsScreen(
    state: StudyPlannerUiState,
    onOpenStopwatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTodosAll = state.subjectProgressList.sumOf { it.totalTodos }
    val completedTodosAll = state.subjectProgressList.sumOf { it.completedTodos }
    val completionPercent = if (totalTodosAll > 0) (completedTodosAll * 100 / totalTodosAll) else 0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Summary KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryKpiCard(
                    title = "오늘 공부",
                    value = TimeFormatter.formatDurationKorean(state.todayTotalStudySeconds),
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiCard(
                    title = "주간 누적",
                    value = TimeFormatter.formatDurationKorean(state.weeklyTotalSeconds),
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiCard(
                    title = "할 일 달성",
                    value = if (totalTodosAll > 0) "${completionPercent}%" else "0%",
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.totalAllTimeStudySeconds == 0L && totalTodosAll == 0) {
            item {
                EmptyStudyStateCard(
                    title = "아직 기록된 공부 시간이 없습니다",
                    subtitle = "스톱워치로 공부를 측정하고 저장하면 캘린더와 차트에 자동으로 시각화됩니다",
                    buttonLabel = "스톱워치로 이동",
                    onActionClick = onOpenStopwatch
                )
            }
        } else {
            // 2. Weekly Stacked Bar Chart
            item {
                WeeklyStackedBarChartCard(
                    bars = state.weeklyChartBars,
                    selectedDate = state.selectedDate
                )
            }

            // 3. Subject Study Share Donut Chart
            item {
                SubjectDonutChartCard(
                    totalSeconds = state.totalAllTimeStudySeconds,
                    subjects = state.subjectProgressList.filter { it.totalStudySeconds > 0L }
                )
            }

            // 4. Subject + Todo Combined Progress Section
            item {
                Text(
                    text = "과목별 공부 시간 · 할 일 달성 현황",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(state.subjectProgressList, key = { it.subjectId ?: -1L }) { item ->
                SubjectCombinedProgressCard(item = item)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SummaryKpiCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeeklyStackedBarChartCard(
    bars: List<DailyChartBar>,
    selectedDate: StudyDate
) {
    val today = StudyDate.today()
    var inspectedIndex by remember(bars, selectedDate) {
        val idx = bars.indexOfFirst { it.date == selectedDate }.takeIf { it >= 0 }
            ?: bars.indexOfFirst { it.date == today }.takeIf { it >= 0 }
            ?: 0
        mutableIntStateOf(idx)
    }
    val maxSeconds = (bars.maxOfOrNull { it.totalSeconds } ?: 0L).coerceAtLeast(60L)
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant
    val inspectedBar = bars.getOrNull(inspectedIndex)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.testTag("weekly_bar_chart_card")
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
                Text("주간 공부 시간 차트", style = MaterialTheme.typography.titleMedium)
                if (inspectedBar != null) {
                    Text(
                        text = "${inspectedBar.date.koreanShortMonthDay()} · ${TimeFormatter.formatDurationKorean(inspectedBar.totalSeconds)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val count = bars.size.coerceAtLeast(1)
                val slotWidth = size.width / count
                val barWidth = (slotWidth * 0.48f).coerceAtMost(28.dp.toPx())

                // Horizontal guide lines
                listOf(0.25f, 0.5f, 0.75f, 1f).forEach { ratio ->
                    val y = size.height * (1f - ratio)
                    drawLine(
                        color = guideColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                bars.forEachIndexed { index, bar ->
                    val left = index * slotWidth + (slotWidth - barWidth) / 2f
                    // Background track
                    drawRoundRect(
                        color = emptyBarColor.copy(alpha = 0.55f),
                        topLeft = Offset(left, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    if (bar.totalSeconds > 0L) {
                        var currentBottom = size.height
                        bar.segments.forEach { segment ->
                            val segHeight = (segment.seconds.toFloat() / maxSeconds.toFloat()) * size.height
                            val top = (currentBottom - segHeight).coerceAtLeast(0f)
                            drawRoundRect(
                                color = Color(segment.colorHex),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, currentBottom - top),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            currentBottom = top
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tappable Day Labels Row
            Row(modifier = Modifier.fillMaxWidth()) {
                bars.forEachIndexed { index, bar ->
                    val isInspected = index == inspectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isInspected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { inspectedIndex = index }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bar.date.koreanDayOfWeekShort(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isInspected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isInspected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            if (inspectedBar != null && inspectedBar.segments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    inspectedBar.segments.take(3).forEach { seg ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(seg.colorHex))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${seg.subjectName} ${TimeFormatter.formatDurationKorean(seg.seconds)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectDonutChartCard(
    totalSeconds: Long,
    subjects: List<SubjectProgressUi>
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.testTag("subject_donut_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("과목별 공부 비중", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isEmpty() || totalSeconds <= 0L) {
                Text(
                    text = "스톱워치로 공부 기록을 저장하면 과목별 비중 차트가 표시됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(144.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 22.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                            var startAngle = -90f
                            subjects.forEach { subj ->
                                val sweep = subj.shareFraction * 360f
                                if (sweep > 0f) {
                                    drawArc(
                                        color = Color(subj.colorHex),
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                    )
                                    startAngle += sweep
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "총 누적",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = TimeFormatter.formatDurationKorean(totalSeconds),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.forEach { subj ->
                            val percent = (subj.shareFraction * 100).toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                            .background(Color(subj.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subj.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${percent}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(subj.colorHex)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectCombinedProgressCard(item: SubjectProgressUi) {
    val subjColor = Color(item.colorHex)
    val todoProgress = if (item.totalTodos > 0) {
        item.completedTodos.toFloat() / item.totalTodos.toFloat()
    } else 0f

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(subjColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.name, style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    text = TimeFormatter.formatDurationKorean(item.totalStudySeconds),
                    style = MaterialTheme.typography.labelLarge,
                    color = subjColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.totalTodos > 0) {
                        "할 일 완료 ${item.completedTodos} / ${item.totalTodos}"
                    } else {
                        "등록된 할 일 없음"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.totalTodos > 0) {
                    Text(
                        text = "${(todoProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item.totalTodos > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { todoProgress },
                    color = subjColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
