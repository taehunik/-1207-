package com.example.ui

import com.example.data.local.DDayEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TodoEntity
import com.example.util.StudyDate

enum class StudyTab(val label: String) {
    CALENDAR("캘린더"),
    STOPWATCH("스톱워치"),
    CHARTS("통계")
}

data class TodoUiModel(
    val todo: TodoEntity,
    val subject: SubjectEntity?,
    val totalStudySeconds: Long
)

data class StudySessionUiModel(
    val session: StudySessionEntity,
    val subject: SubjectEntity?,
    val todoTitle: String?
)

data class DDayUiModel(
    val entity: DDayEntity,
    val targetDate: StudyDate,
    val daysRemaining: Int
)

data class CalendarDaySummary(
    val dateIso: String,
    val totalStudySeconds: Long,
    val totalTodos: Int,
    val completedTodos: Int,
    val subjectColors: List<Long>
)

data class SubjectSegment(
    val subjectId: Long?,
    val subjectName: String,
    val colorHex: Long,
    val seconds: Long
)

data class DailyChartBar(
    val date: StudyDate,
    val totalSeconds: Long,
    val segments: List<SubjectSegment>
)

data class SubjectProgressUi(
    val subjectId: Long?,
    val name: String,
    val colorHex: Long,
    val totalStudySeconds: Long,
    val shareFraction: Float,
    val totalTodos: Int,
    val completedTodos: Int
)

data class StopwatchUiState(
    val isRunning: Boolean = false,
    val elapsedMillis: Long = 0L,
    val selectedSubjectId: Long? = null,
    val selectedTodoId: Long? = null
)

data class StudyPlannerUiState(
    val isDarkMode: Boolean = false,
    val currentTab: StudyTab = StudyTab.CALENDAR,
    val selectedDate: StudyDate = StudyDate.today(),
    val calendarYear: Int = StudyDate.today().year,
    val calendarMonth: Int = StudyDate.today().month,
    val subjects: List<SubjectEntity> = emptyList(),
    val todosForToday: List<TodoUiModel> = emptyList(),
    val todosForSelectedDate: List<TodoUiModel> = emptyList(),
    val sessionsForSelectedDate: List<StudySessionUiModel> = emptyList(),
    val todaySessions: List<StudySessionUiModel> = emptyList(),
    val todayTotalStudySeconds: Long = 0L,
    val selectedDateTotalStudySeconds: Long = 0L,
    val ddays: List<DDayUiModel> = emptyList(),
    val featuredDDay: DDayUiModel? = null,
    val calendarDaySummaries: Map<String, CalendarDaySummary> = emptyMap(),
    val weeklyChartBars: List<DailyChartBar> = emptyList(),
    val weeklyTotalSeconds: Long = 0L,
    val subjectProgressList: List<SubjectProgressUi> = emptyList(),
    val totalAllTimeStudySeconds: Long = 0L,
    val stopwatch: StopwatchUiState = StopwatchUiState()
)
