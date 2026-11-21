package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.DDayUiModel
import com.example.ui.StopwatchUiState
import com.example.ui.TodoUiModel
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.TimeFormatter

@Composable
fun FeaturedDDayHeroCard(
    featuredDDay: DDayUiModel?,
    totalDDayCount: Int,
    onManageClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredDDay == null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onAddClick)
                .testTag("featured_dday_empty_card"),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
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
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "D-Day 없음",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "등록된 D-Day가 없습니다",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "시험이나 목표일을 등록해 대표 D-Day로 확인하세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                FilledTonalButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("add_dday_hero_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("추가")
                }
            }
        }
    } else {
        val accentColor = Color(featuredDDay.entity.colorHex)
        val badgeText = TimeFormatter.formatDDayBadge(featuredDDay.daysRemaining)
        val gradient = Brush.linearGradient(
            colors = listOf(
                accentColor,
                accentColor.copy(alpha = 0.80f)
            )
        )

        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onManageClick)
                .testTag("featured_dday_card"),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "대표 D-Day",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (totalDDayCount > 1) {
                                    "대표 D-DAY · 전체 ${totalDDayCount}개"
                                } else {
                                    "대표 D-DAY"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = featuredDDay.entity.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${featuredDDay.targetDate.koreanYearMonthDayLabel()} 목표",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveStopwatchBanner(
    stopwatch: StopwatchUiState,
    subjectName: String?,
    subjectColorHex: Long?,
    onTogglePlayPause: () -> Unit,
    onOpenStopwatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!stopwatch.isRunning && stopwatch.elapsedMillis <= 0L) return

    val dotColor = subjectColorHex?.let { Color(it) } ?: MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onOpenStopwatch)
            .testTag("active_stopwatch_banner"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
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
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = buildString {
                            append(subjectName ?: "공부")
                            append(if (stopwatch.isRunning) " · 측정 중" else " · 일시정지")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = TimeFormatter.formatStopwatchMillis(stopwatch.elapsedMillis),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.testTag("banner_stopwatch_toggle")
                ) {
                    Icon(
                        imageVector = if (stopwatch.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (stopwatch.isRunning) "스톱워치 일시정지" else "스톱워치 계속하기",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                FilledTonalButton(
                    onClick = onOpenStopwatch,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("열기")
                }
            }
        }
    }
}

@Composable
fun TodoItemCard(
    item: TodoUiModel,
    onToggleComplete: () -> Unit,
    onStartStopwatch: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (item.todo.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "todo_card_bg"
    )
    val subjectColor = item.subject?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.outline

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("todo_item_${item.todo.id}"),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.testTag("todo_toggle_${item.todo.id}")
            ) {
                Icon(
                    imageVector = if (item.todo.isCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = if (item.todo.isCompleted) "완료됨" else "완료 표시",
                    tint = if (item.todo.isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.todo.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (item.todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (item.todo.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.subject != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = subjectColor.copy(alpha = 0.14f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = item.subject.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = subjectColor
                                )
                            }
                        }
                    }

                    if (item.totalStudySeconds > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = TimeFormatter.formatDurationKorean(item.totalStudySeconds),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onStartStopwatch,
                modifier = Modifier.testTag("todo_focus_${item.todo.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "이 할 일로 스톱워치 시작",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("todo_delete_${item.todo.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "할 일 삭제",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyStudyStateCard(
    title: String,
    subtitle: String,
    buttonLabel: String,
    onActionClick: () -> Unit,
    showIllustration: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showIllustration) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_desk),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(124.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("empty_state_action_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonLabel)
            }
        }
    }
}
