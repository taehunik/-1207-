package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.CalendarDaySummary
import com.example.util.StudyDate
import com.example.util.TimeFormatter

@Composable
fun MonthlyStudyCalendarCard(
    calendarYear: Int,
    calendarMonth: Int,
    selectedDate: StudyDate,
    selectedDateTotalStudySeconds: Long,
    calendarDaySummaries: Map<String, CalendarDaySummary>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onSelectDate: (StudyDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = StudyDate.today()
    val monthGrid = StudyDate.daysInMonthGrid(calendarYear, calendarMonth)
    val dayHeaders = listOf("일", "월", "화", "수", "목", "금", "토")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Month & Year Navigation + Today shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier.testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "이전 달"
                        )
                    }
                    Column {
                        Text(
                            text = "${calendarYear}년 ${calendarMonth}월",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (selectedDateTotalStudySeconds > 0L) {
                                "${selectedDate.koreanShortMonthDay()} 공부 ${TimeFormatter.formatDurationKorean(selectedDateTotalStudySeconds)}"
                            } else {
                                "공부 전용 캘린더"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedDateTotalStudySeconds > 0L) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "다음 달"
                        )
                    }
                }
                TextButton(
                    onClick = onJumpToToday,
                    modifier = Modifier.testTag("calendar_jump_today")
                ) {
                    Text("오늘")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekday Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEachIndexed { idx, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = when (idx) {
                            0 -> MaterialTheme.colorScheme.error
                            6 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Full Month Calendar Rows
            val weeks = monthGrid.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEach { date ->
                            if (date == null) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            } else {
                                val summary = calendarDaySummaries[date.toIsoString()]
                                val studySecs = summary?.totalStudySeconds ?: 0L
                                val isSelected = date == selectedDate
                                val isToday = date == today

                                val heatAlpha = when {
                                    studySecs >= 7200L -> 0.28f
                                    studySecs >= 3600L -> 0.20f
                                    studySecs >= 1200L -> 0.14f
                                    studySecs > 0L -> 0.08f
                                    else -> 0f
                                }

                                val cellBg = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    heatAlpha > 0f -> MaterialTheme.colorScheme.secondary.copy(alpha = heatAlpha)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cellBg)
                                        .border(
                                            width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                            color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectDate(date) }
                                        .padding(vertical = 3.dp)
                                        .testTag("calendar_cell_${date.toIsoString()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = date.day.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                date.dayOfWeekSundayFirst() == 0 -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        val dots = summary?.subjectColors.orEmpty()
                                        if (dots.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                dots.take(3).forEach { hex ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isSelected) Color.White else Color(hex)
                                                            )
                                                    )
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(5.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "공부 시간 농도",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "과목 활동",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
