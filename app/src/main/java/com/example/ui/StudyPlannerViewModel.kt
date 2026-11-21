package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DDayEntity
import com.example.data.local.StudyPlannerDatabase
import com.example.data.local.StudySessionEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TodoEntity
import com.example.data.preferences.AppPreferences
import com.example.data.preferences.PersistedStopwatchState
import com.example.data.repository.StudyPlannerRepository
import com.example.ui.theme.SubjectPalette
import com.example.util.StudyDate
import com.example.util.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StudyPlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyPlannerRepository

    init {
        val db = StudyPlannerDatabase.getInstance(application)
        val prefs = AppPreferences(application)
        repository = StudyPlannerRepository(db.studyPlannerDao(), prefs)
    }

    private val _currentTab = MutableStateFlow(StudyTab.CALENDAR)
    private val _selectedDate = MutableStateFlow(StudyDate.today())
    private val _calendarMonth = MutableStateFlow(StudyDate.today().year to StudyDate.today().month)

    private val _stopwatchRunning = MutableStateFlow(false)
    private val _stopwatchAccumulatedMs = MutableStateFlow(0L)
    private val _stopwatchStartWallMs = MutableStateFlow(0L)
    private val _stopwatchElapsedMs = MutableStateFlow(0L)
    private val _stopwatchSubjectId = MutableStateFlow<Long?>(null)
    private val _stopwatchTodoId = MutableStateFlow<Long?>(null)

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private var tickerJob: Job? = null

    private data class NavigationSnapshot(
        val tab: StudyTab,
        val selectedDate: StudyDate,
        val calendarMonth: Pair<Int, Int>
    )

    private val navFlow = combine(_currentTab, _selectedDate, _calendarMonth) { tab, date, ym ->
        NavigationSnapshot(tab, date, ym)
    }

    private val stopwatchFlow = combine(
        _stopwatchRunning,
        _stopwatchElapsedMs,
        _stopwatchSubjectId,
        _stopwatchTodoId
    ) { running, elapsed, subjectId, todoId ->
        StopwatchUiState(
            isRunning = running,
            elapsedMillis = elapsed,
            selectedSubjectId = subjectId,
            selectedTodoId = todoId
        )
    }

    private data class RawDatabaseSnapshot(
        val subjects: List<SubjectEntity>,
        val todos: List<TodoEntity>,
        val sessions: List<StudySessionEntity>,
        val ddays: List<DDayEntity>
    )

    private val dbFlow = combine(
        repository.subjects,
        repository.allTodos,
        repository.allSessions,
        repository.ddays
    ) { subjects, todos, sessions, ddays ->
        RawDatabaseSnapshot(subjects, todos, sessions, ddays)
    }

    val uiState: StateFlow<StudyPlannerUiState> = combine(
        repository.isDarkMode,
        navFlow,
        dbFlow,
        stopwatchFlow
    ) { isDark, nav, db, stopwatch ->
        val today = StudyDate.today()
        val todayIso = today.toIsoString()
        val selectedIso = nav.selectedDate.toIsoString()

        val subjectMap = db.subjects.associateBy { it.id }
        val todoMap = db.todos.associateBy { it.id }

        val validSubjectId = stopwatch.selectedSubjectId?.takeIf { subjectMap.containsKey(it) }
            ?: db.subjects.firstOrNull()?.id
        val validTodoId = stopwatch.selectedTodoId?.takeIf { todoMap.containsKey(it) }
        val normalizedStopwatch = stopwatch.copy(
            selectedSubjectId = validSubjectId,
            selectedTodoId = validTodoId
        )

        val secondsByTodo = db.sessions
            .filter { it.todoId != null }
            .groupBy { it.todoId!! }
            .mapValues { (_, list) -> list.sumOf { it.durationSeconds } }

        val todosForToday = db.todos
            .filter { it.dateIso == todayIso }
            .map { todo ->
                TodoUiModel(
                    todo = todo,
                    subject = todo.subjectId?.let { subjectMap[it] },
                    totalStudySeconds = secondsByTodo[todo.id] ?: 0L
                )
            }

        val todosForSelectedDate = db.todos
            .filter { it.dateIso == selectedIso }
            .map { todo ->
                TodoUiModel(
                    todo = todo,
                    subject = todo.subjectId?.let { subjectMap[it] },
                    totalStudySeconds = secondsByTodo[todo.id] ?: 0L
                )
            }

        val sessionsForSelectedDate = db.sessions
            .filter { it.dateIso == selectedIso }
            .map { session ->
                StudySessionUiModel(
                    session = session,
                    subject = session.subjectId?.let { subjectMap[it] },
                    todoTitle = session.todoId?.let { todoMap[it]?.title }
                )
            }

        val todaySessions = db.sessions
            .filter { it.dateIso == todayIso }
            .map { session ->
                StudySessionUiModel(
                    session = session,
                    subject = session.subjectId?.let { subjectMap[it] },
                    todoTitle = session.todoId?.let { todoMap[it]?.title }
                )
            }

        val todayTotalSeconds = todaySessions.sumOf { it.session.durationSeconds }
        val selectedTotalSeconds = sessionsForSelectedDate.sumOf { it.session.durationSeconds }

        val ddayModels = db.ddays.map { entity ->
            val targetDate = StudyDate.parse(entity.targetDateIso)
            DDayUiModel(
                entity = entity,
                targetDate = targetDate,
                daysRemaining = today.daysUntil(targetDate)
            )
        }
        val featuredDDay = ddayModels.firstOrNull { it.entity.isFeatured } ?: ddayModels.firstOrNull()

        // Calendar summaries per ISO date
        val allDates = (db.todos.map { it.dateIso } + db.sessions.map { it.dateIso }).distinct()
        val sessionsByDate = db.sessions.groupBy { it.dateIso }
        val todosByDate = db.todos.groupBy { it.dateIso }

        val calendarSummaries = allDates.associateWith { dateIso ->
            val daySessions = sessionsByDate[dateIso].orEmpty()
            val dayTodos = todosByDate[dateIso].orEmpty()
            val colors = (
                daySessions.mapNotNull { it.subjectId?.let { sid -> subjectMap[sid]?.colorHex } } +
                    dayTodos.mapNotNull { it.subjectId?.let { sid -> subjectMap[sid]?.colorHex } }
                ).distinct().take(4)

            CalendarDaySummary(
                dateIso = dateIso,
                totalStudySeconds = daySessions.sumOf { it.durationSeconds },
                totalTodos = dayTodos.size,
                completedTodos = dayTodos.count { it.isCompleted },
                subjectColors = colors
            )
        }

        // Weekly chart bars (Sun..Sat week containing selectedDate)
        val weekDates = StudyDate.weekDatesAround(nav.selectedDate)
        val weeklyBars = weekDates.map { date ->
            val iso = date.toIsoString()
            val daySessions = sessionsByDate[iso].orEmpty()
            val grouped = daySessions.groupBy { it.subjectId }
            val segments = grouped.entries
                .sortedByDescending { (_, list) -> list.sumOf { it.durationSeconds } }
                .map { (subjectId, list) ->
                    val subj = subjectId?.let { subjectMap[it] }
                    SubjectSegment(
                        subjectId = subjectId,
                        subjectName = subj?.name ?: "일반 공부",
                        colorHex = subj?.colorHex ?: 0xFF868E96L,
                        seconds = list.sumOf { it.durationSeconds }
                    )
                }
            DailyChartBar(
                date = date,
                totalSeconds = daySessions.sumOf { it.durationSeconds },
                segments = segments
            )
        }
        val weeklyTotalSeconds = weeklyBars.sumOf { it.totalSeconds }

        // Subject progress (total study time + todo completion per subject)
        val totalAllTimeSeconds = db.sessions.sumOf { it.durationSeconds }
        val sessionsBySubject = db.sessions.groupBy { it.subjectId }
        val todosBySubject = db.todos.groupBy { it.subjectId }

        val subjectIdsToRender = (
            db.subjects.map { it.id as Long? } +
                sessionsBySubject.keys +
                todosBySubject.keys
            ).distinct()

        val subjectProgressList = subjectIdsToRender.mapNotNull { subjectId ->
            val subject = subjectId?.let { subjectMap[it] }
            if (subjectId != null && subject == null) return@mapNotNull null

            val subjSessions = sessionsBySubject[subjectId].orEmpty()
            val subjTodos = todosBySubject[subjectId].orEmpty()
            val subjSeconds = subjSessions.sumOf { it.durationSeconds }

            // Only show "일반 공부" (null) if there is actually data for it
            if (subjectId == null && subjSessions.isEmpty() && subjTodos.isEmpty()) {
                return@mapNotNull null
            }

            SubjectProgressUi(
                subjectId = subjectId,
                name = subject?.name ?: "일반 공부",
                colorHex = subject?.colorHex ?: 0xFF868E96L,
                totalStudySeconds = subjSeconds,
                shareFraction = if (totalAllTimeSeconds > 0L) {
                    subjSeconds.toFloat() / totalAllTimeSeconds.toFloat()
                } else 0f,
                totalTodos = subjTodos.size,
                completedTodos = subjTodos.count { it.isCompleted }
            )
        }.sortedByDescending { it.totalStudySeconds }

        StudyPlannerUiState(
            isDarkMode = isDark,
            currentTab = nav.tab,
            selectedDate = nav.selectedDate,
            calendarYear = nav.calendarMonth.first,
            calendarMonth = nav.calendarMonth.second,
            subjects = db.subjects,
            todosForToday = todosForToday,
            todosForSelectedDate = todosForSelectedDate,
            sessionsForSelectedDate = sessionsForSelectedDate,
            todaySessions = todaySessions,
            todayTotalStudySeconds = todayTotalSeconds,
            selectedDateTotalStudySeconds = selectedTotalSeconds,
            ddays = ddayModels,
            featuredDDay = featuredDDay,
            calendarDaySummaries = calendarSummaries,
            weeklyChartBars = weeklyBars,
            weeklyTotalSeconds = weeklyTotalSeconds,
            subjectProgressList = subjectProgressList,
            totalAllTimeStudySeconds = totalAllTimeSeconds,
            stopwatch = normalizedStopwatch
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyPlannerUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultSubjectsIfFirstLaunch()
            val savedStopwatch = repository.loadStopwatchState()
            _stopwatchAccumulatedMs.value = savedStopwatch.accumulatedMillis
            _stopwatchStartWallMs.value = savedStopwatch.startWallTimeMillis
            _stopwatchSubjectId.value = savedStopwatch.subjectId
            _stopwatchTodoId.value = savedStopwatch.todoId
            _stopwatchRunning.value = savedStopwatch.isRunning

            if (savedStopwatch.isRunning && savedStopwatch.startWallTimeMillis > 0L) {
                val now = System.currentTimeMillis()
                val delta = (now - savedStopwatch.startWallTimeMillis).coerceAtLeast(0L)
                _stopwatchElapsedMs.value = savedStopwatch.accumulatedMillis + delta
                startTickerLoop()
            } else {
                _stopwatchElapsedMs.value = savedStopwatch.accumulatedMillis
            }
        }
    }

    fun selectTab(tab: StudyTab) {
        _currentTab.value = tab
    }

    fun selectDate(date: StudyDate) {
        _selectedDate.value = date
        _calendarMonth.value = date.year to date.month
    }

    fun jumpToToday() {
        val today = StudyDate.today()
        _selectedDate.value = today
        _calendarMonth.value = today.year to today.month
    }

    fun previousMonth() {
        val (year, month) = _calendarMonth.value
        _calendarMonth.value = if (month == 1) (year - 1) to 12 else year to (month - 1)
    }

    fun nextMonth() {
        val (year, month) = _calendarMonth.value
        _calendarMonth.value = if (month == 12) (year + 1) to 1 else year to (month + 1)
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    fun addSubject(name: String, colorHex: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = repository.addSubject(name, colorHex)
            if (_stopwatchSubjectId.value == null) {
                _stopwatchSubjectId.value = newId
                persistCurrentStopwatch()
            }
            _snackbarMessages.tryEmit("과목 '${name.trim()}'이(가) 추가되었습니다")
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            if (_stopwatchSubjectId.value == subject.id) {
                _stopwatchSubjectId.value = null
                persistCurrentStopwatch()
            }
        }
    }

    fun addTodo(title: String, subjectId: Long?, date: StudyDate = _selectedDate.value) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTodo(title, subjectId, date.toIsoString())
        }
    }

    fun toggleTodoCompleted(todo: TodoEntity) {
        viewModelScope.launch {
            repository.toggleTodoCompleted(todo.id, !todo.isCompleted)
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
            if (_stopwatchTodoId.value == todo.id) {
                _stopwatchTodoId.value = null
                persistCurrentStopwatch()
            }
        }
    }

    fun addDDay(title: String, targetDate: StudyDate, isFeatured: Boolean, colorHex: Long = SubjectPalette[2]) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addDDay(title, targetDate.toIsoString(), isFeatured, colorHex)
            _snackbarMessages.tryEmit("D-Day '${title.trim()}'이(가) 등록되었습니다")
        }
    }

    fun setFeaturedDDay(ddayId: Long) {
        viewModelScope.launch {
            repository.setFeaturedDDay(ddayId)
            _snackbarMessages.tryEmit("대표 D-Day가 변경되었습니다")
        }
    }

    fun deleteDDay(dday: DDayEntity) {
        viewModelScope.launch {
            repository.deleteDDay(dday)
        }
    }

    fun deleteStudySession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.deleteStudySession(session)
        }
    }

    // Stopwatch controls
    fun selectStopwatchSubject(subjectId: Long?) {
        _stopwatchSubjectId.value = subjectId
        // Clear todo selection if it belongs to a different subject
        val currentTodoId = _stopwatchTodoId.value
        if (currentTodoId != null) {
            val todo = uiState.value.todosForSelectedDate.firstOrNull { it.todo.id == currentTodoId }
            if (todo != null && todo.todo.subjectId != subjectId) {
                _stopwatchTodoId.value = null
            }
        }
        viewModelScope.launch { persistCurrentStopwatch() }
    }

    fun selectStopwatchTodo(todoId: Long?) {
        _stopwatchTodoId.value = todoId
        if (todoId != null) {
            val matched = uiState.value.todosForToday.firstOrNull { it.todo.id == todoId }
                ?: uiState.value.todosForSelectedDate.firstOrNull { it.todo.id == todoId }
            if (matched?.todo?.subjectId != null) {
                _stopwatchSubjectId.value = matched.todo.subjectId
            }
        }
        viewModelScope.launch { persistCurrentStopwatch() }
    }

    fun startStopwatchForTodo(todoUi: TodoUiModel) {
        _stopwatchSubjectId.value = todoUi.todo.subjectId ?: _stopwatchSubjectId.value
        _stopwatchTodoId.value = todoUi.todo.id
        if (!_stopwatchRunning.value) {
            startStopwatch()
        } else {
            viewModelScope.launch { persistCurrentStopwatch() }
        }
        _currentTab.value = StudyTab.STOPWATCH
    }

    fun startStopwatch() {
        if (_stopwatchRunning.value) return
        val now = System.currentTimeMillis()
        _stopwatchStartWallMs.value = now
        _stopwatchRunning.value = true
        startTickerLoop()
        viewModelScope.launch { persistCurrentStopwatch() }
    }

    fun pauseStopwatch() {
        if (!_stopwatchRunning.value) return
        val now = System.currentTimeMillis()
        val delta = (now - _stopwatchStartWallMs.value).coerceAtLeast(0L)
        val total = _stopwatchAccumulatedMs.value + delta
        _stopwatchAccumulatedMs.value = total
        _stopwatchElapsedMs.value = total
        _stopwatchStartWallMs.value = 0L
        _stopwatchRunning.value = false
        tickerJob?.cancel()
        tickerJob = null
        viewModelScope.launch { persistCurrentStopwatch() }
    }

    fun resetStopwatch() {
        tickerJob?.cancel()
        tickerJob = null
        _stopwatchRunning.value = false
        _stopwatchAccumulatedMs.value = 0L
        _stopwatchStartWallMs.value = 0L
        _stopwatchElapsedMs.value = 0L
        viewModelScope.launch {
            persistCurrentStopwatch()
        }
    }

    fun saveStopwatchSession(memo: String = "") {
        val currentMs = if (_stopwatchRunning.value) {
            val delta = (System.currentTimeMillis() - _stopwatchStartWallMs.value).coerceAtLeast(0L)
            _stopwatchAccumulatedMs.value + delta
        } else {
            _stopwatchAccumulatedMs.value
        }

        val durationSeconds = currentMs / 1000L
        if (durationSeconds < 1L) {
            _snackbarMessages.tryEmit("1초 이상 측정 후 저장할 수 있습니다")
            return
        }

        val subjectId = _stopwatchSubjectId.value ?: uiState.value.subjects.firstOrNull()?.id
        val todoId = _stopwatchTodoId.value
        val todayIso = StudyDate.today().toIsoString()

        tickerJob?.cancel()
        tickerJob = null
        _stopwatchRunning.value = false
        _stopwatchAccumulatedMs.value = 0L
        _stopwatchStartWallMs.value = 0L
        _stopwatchElapsedMs.value = 0L

        viewModelScope.launch {
            repository.addStudySession(
                subjectId = subjectId,
                todoId = todoId,
                dateIso = todayIso,
                durationSeconds = durationSeconds,
                memo = memo
            )
            persistCurrentStopwatch()
            _snackbarMessages.tryEmit(
                "${TimeFormatter.formatDurationKorean(durationSeconds)} 기록이 캘린더·차트에 저장되었습니다"
            )
        }
    }

    fun resetAllData() {
        tickerJob?.cancel()
        tickerJob = null
        _stopwatchRunning.value = false
        _stopwatchAccumulatedMs.value = 0L
        _stopwatchStartWallMs.value = 0L
        _stopwatchElapsedMs.value = 0L
        _stopwatchSubjectId.value = null
        _stopwatchTodoId.value = null
        _selectedDate.value = StudyDate.today()
        _calendarMonth.value = StudyDate.today().year to StudyDate.today().month

        viewModelScope.launch {
            repository.resetAllData()
            _snackbarMessages.tryEmit("모든 공부 데이터가 초기화되었습니다")
        }
    }

    private fun startTickerLoop() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive && _stopwatchRunning.value) {
                val now = System.currentTimeMillis()
                val delta = (now - _stopwatchStartWallMs.value).coerceAtLeast(0L)
                _stopwatchElapsedMs.value = _stopwatchAccumulatedMs.value + delta
                delay(200L)
            }
        }
    }

    private suspend fun persistCurrentStopwatch() {
        repository.saveStopwatchState(
            PersistedStopwatchState(
                isRunning = _stopwatchRunning.value,
                accumulatedMillis = _stopwatchAccumulatedMs.value,
                startWallTimeMillis = _stopwatchStartWallMs.value,
                subjectId = _stopwatchSubjectId.value,
                todoId = _stopwatchTodoId.value
            )
        )
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
