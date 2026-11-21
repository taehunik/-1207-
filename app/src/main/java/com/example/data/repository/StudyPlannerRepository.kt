package com.example.data.repository

import com.example.data.local.DDayEntity
import com.example.data.local.StudyPlannerDao
import com.example.data.local.StudySessionEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TodoEntity
import com.example.data.preferences.AppPreferences
import com.example.data.preferences.PersistedStopwatchState
import com.example.ui.theme.SubjectPalette
import kotlinx.coroutines.flow.Flow

class StudyPlannerRepository(
    private val dao: StudyPlannerDao,
    private val preferences: AppPreferences
) {
    val isDarkMode: Flow<Boolean> = preferences.isDarkModeFlow
    val subjects: Flow<List<SubjectEntity>> = dao.observeSubjects()
    val allTodos: Flow<List<TodoEntity>> = dao.observeAllTodos()
    val allSessions: Flow<List<StudySessionEntity>> = dao.observeAllSessions()
    val ddays: Flow<List<DDayEntity>> = dao.observeDDays()

    suspend fun ensureDefaultSubjectsIfFirstLaunch() {
        if (!preferences.hasSeededDefaultSubjects() && dao.getSubjectCount() == 0) {
            val defaults = listOf(
                SubjectEntity(name = "국어", colorHex = SubjectPalette[0]),
                SubjectEntity(name = "영어", colorHex = SubjectPalette[1]),
                SubjectEntity(name = "수학", colorHex = SubjectPalette[2]),
                SubjectEntity(name = "전공/탐구", colorHex = SubjectPalette[3])
            )
            defaults.forEach { dao.insertSubject(it) }
            preferences.markDefaultSubjectsSeeded()
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        preferences.setDarkMode(enabled)
    }

    suspend fun addSubject(name: String, colorHex: Long): Long {
        return dao.insertSubject(SubjectEntity(name = name.trim(), colorHex = colorHex))
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        dao.deleteSubject(subject)
    }

    suspend fun addTodo(title: String, subjectId: Long?, dateIso: String): Long {
        return dao.insertTodo(
            TodoEntity(
                title = title.trim(),
                subjectId = subjectId,
                dateIso = dateIso
            )
        )
    }

    suspend fun toggleTodoCompleted(todoId: Long, completed: Boolean) {
        dao.setTodoCompleted(todoId, completed)
    }

    suspend fun updateTodo(todo: TodoEntity) {
        dao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: TodoEntity) {
        dao.deleteTodo(todo)
    }

    suspend fun addStudySession(
        subjectId: Long?,
        todoId: Long?,
        dateIso: String,
        durationSeconds: Long,
        memo: String = ""
    ): Long {
        return dao.insertSession(
            StudySessionEntity(
                subjectId = subjectId,
                todoId = todoId,
                dateIso = dateIso,
                durationSeconds = durationSeconds,
                memo = memo.trim()
            )
        )
    }

    suspend fun deleteStudySession(session: StudySessionEntity) {
        dao.deleteSession(session)
    }

    suspend fun addDDay(title: String, targetDateIso: String, isFeatured: Boolean, colorHex: Long): Long {
        return dao.insertDDayEnsuringFeatured(
            DDayEntity(
                title = title.trim(),
                targetDateIso = targetDateIso,
                isFeatured = isFeatured,
                colorHex = colorHex
            )
        )
    }

    suspend fun setFeaturedDDay(ddayId: Long) {
        dao.setFeaturedDDay(ddayId)
    }

    suspend fun deleteDDay(dday: DDayEntity) {
        dao.deleteDDayAndReassignFeatured(dday)
    }

    suspend fun loadStopwatchState(): PersistedStopwatchState = preferences.loadStopwatchState()

    suspend fun saveStopwatchState(state: PersistedStopwatchState) = preferences.saveStopwatchState(state)

    suspend fun clearStopwatchState() = preferences.clearStopwatchState()

    suspend fun resetAllData() {
        dao.clearAllData()
        preferences.clearStopwatchState()
    }
}
