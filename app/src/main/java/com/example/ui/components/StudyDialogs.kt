package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.DDayEntity
import com.example.data.local.SubjectEntity
import com.example.ui.DDayUiModel
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SubjectPalette
import com.example.util.StudyDate
import com.example.util.TimeFormatter
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTodoDialog(
    selectedDate: StudyDate,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAddTodo: (title: String, subjectId: Long?) -> Unit,
    onRequestAddSubject: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubjectId by remember(subjects) {
        mutableStateOf(subjects.firstOrNull()?.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("할 일 추가", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedDate.koreanYearMonthDayLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("공부할 내용") },
                    placeholder = { Text("예: 모의고사 1회 풀기") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_todo_title_input")
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "과목 선택",
                        style = MaterialTheme.typography.labelLarge
                    )
                    TextButton(onClick = onRequestAddSubject) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("과목 추가")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        val isSelected = selectedSubjectId == subject.id
                        val subjColor = Color(subject.colorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSubjectId = if (isSelected) null else subject.id
                            },
                            label = { Text(subject.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(subjColor)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTodo(title, selectedSubjectId)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_todo_button")
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onAddSubject: (name: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(SubjectPalette.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 과목 추가") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("과목 이름") },
                    placeholder = { Text("예: 알고리즘, 토익, 물리") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_subject_name_input")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("색상 태그", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubjectPalette.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "선택됨",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddSubject(name, selectedColor)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_subject_button")
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDDayDialog(
    onDismiss: () -> Unit,
    onAddDDay: (title: String, targetDate: StudyDate, isFeatured: Boolean, colorHex: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf(StudyDate.today().plusDays(14)) }
    var isFeatured by remember { mutableStateOf(true) }
    var selectedColor by remember { mutableLongStateOf(SubjectPalette[2]) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val initialMillis = remember(targetDate) {
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(targetDate.year, targetDate.month - 1, targetDate.day, 12, 0, 0)
            }.timeInMillis
        }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            targetDate = StudyDate(
                                year = utcCal.get(Calendar.YEAR),
                                month = utcCal.get(Calendar.MONTH) + 1,
                                day = utcCal.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("선택")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("D-Day 등록") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("시험 또는 목표 이름") },
                    placeholder = { Text("예: 기말고사, 정보처리기사 실기") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_dday_title_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { showDatePicker = true }
                        .testTag("dday_pick_date_button"),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "목표 날짜",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = targetDate.koreanYearMonthDayLabel(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TimeFormatter.formatDDayBadge(StudyDate.today().daysUntil(targetDate)),
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "날짜 선택",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("대표 D-Day로 표시", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "홈 화면 최상단 배너에 고정됩니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("배너 색상", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubjectPalette.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "선택됨",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddDDay(title, targetDate, isFeatured, selectedColor)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_dday_button")
            ) {
                Text("등록")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDayManagerSheet(
    ddays: List<DDayUiModel>,
    onDismiss: () -> Unit,
    onAddNewClick: () -> Unit,
    onSetFeatured: (Long) -> Unit,
    onDeleteDDay: (DDayEntity) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("D-Day 관리", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "별표(★)를 누르면 대표 D-Day로 홈에 고정됩니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onAddNewClick,
                    modifier = Modifier.testTag("dday_sheet_add_button")
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

            Spacer(modifier = Modifier.height(16.dp))

            if (ddays.isEmpty()) {
                EmptyStudyStateCard(
                    title = "등록된 D-Day가 없습니다",
                    subtitle = "중요한 시험이나 목표일을 등록해 남은 일수를 확인해 보세요",
                    buttonLabel = "첫 D-Day 추가하기",
                    onActionClick = onAddNewClick,
                    showIllustration = false
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    items(ddays, key = { it.entity.id }) { item ->
                        val accentColor = Color(item.entity.colorHex)
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (item.entity.isFeatured) {
                                accentColor.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (item.entity.isFeatured) 1.5.dp else 1.dp,
                                color = if (item.entity.isFeatured) accentColor else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onSetFeatured(item.entity.id) },
                                    modifier = Modifier.testTag("dday_star_${item.entity.id}")
                                ) {
                                    Icon(
                                        imageVector = if (item.entity.isFeatured) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = if (item.entity.isFeatured) "대표 D-Day" else "대표 D-Day로 설정",
                                        tint = if (item.entity.isFeatured) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.entity.title,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        if (item.entity.isFeatured) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = accentColor
                                            ) {
                                                Text(
                                                    text = "대표",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.targetDate.koreanYearMonthDayLabel(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = TimeFormatter.formatDDayBadge(item.daysRemaining),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = accentColor
                                )
                                IconButton(
                                    onClick = { onDeleteDDay(item.entity) },
                                    modifier = Modifier.testTag("dday_delete_${item.entity.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "D-Day 삭제",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    isDarkMode: Boolean,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onAddSubjectClick: () -> Unit,
    onDeleteSubject: (SubjectEntity) -> Unit,
    onResetAllData: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("전체 데이터 초기화") },
            text = {
                Text("Room 데이터베이스에 저장된 모든 과목, 할 일, 스톱워치 공부 기록, D-Day가 삭제됩니다. 계속할까요?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetAllData()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_reset_all_data_button")
                ) {
                    Text("초기화")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text("설정 및 관리", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Explicit In-App Dark Mode Toggle
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "다크 모드 아이콘",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "다크 모드 (${if (isDarkMode) "켜짐" else "꺼짐"})",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "시스템 설정과 별개로 앱 내 스위치로 직접 켜고 끌 수 있습니다",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChange,
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subjects Management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("과목 관리", style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = onAddSubjectClick,
                    modifier = Modifier.testTag("settings_add_subject_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("과목 추가")
                }
            }

            if (subjects.isEmpty()) {
                Text(
                    text = "등록된 과목이 없습니다. 과목을 추가해 할 일과 스톱워치를 연동해 보세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        val color = Color(subject.colorHex)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = color.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = color
                                )
                                IconButton(
                                    onClick = { onDeleteSubject(subject) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "${subject.name} 삭제",
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(18.dp))

            // Full Data Reset Button
            Text("데이터 관리", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "테스트 중 데이터가 꼬이거나 새 학기를 시작할 때 모든 Room 데이터를 초기화합니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("settings_reset_all_data_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("전체 데이터 초기화")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
