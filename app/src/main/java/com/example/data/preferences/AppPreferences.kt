package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.studyPlannerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "study_planner_prefs"
)

data class PersistedStopwatchState(
    val isRunning: Boolean = false,
    val accumulatedMillis: Long = 0L,
    val startWallTimeMillis: Long = 0L,
    val subjectId: Long? = null,
    val todoId: Long? = null
)

class AppPreferences(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
        val SEEDED_DEFAULT_SUBJECTS = booleanPreferencesKey("seeded_default_subjects")
        val SW_RUNNING = booleanPreferencesKey("sw_running")
        val SW_ACCUMULATED_MS = longPreferencesKey("sw_accumulated_ms")
        val SW_START_WALL_MS = longPreferencesKey("sw_start_wall_ms")
        val SW_SUBJECT_ID = longPreferencesKey("sw_subject_id")
        val SW_TODO_ID = longPreferencesKey("sw_todo_id")
    }

    val isDarkModeFlow: Flow<Boolean> = context.studyPlannerDataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.studyPlannerDataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun hasSeededDefaultSubjects(): Boolean {
        return context.studyPlannerDataStore.data.first()[Keys.SEEDED_DEFAULT_SUBJECTS] ?: false
    }

    suspend fun markDefaultSubjectsSeeded() {
        context.studyPlannerDataStore.edit { prefs ->
            prefs[Keys.SEEDED_DEFAULT_SUBJECTS] = true
        }
    }

    suspend fun loadStopwatchState(): PersistedStopwatchState {
        val prefs = context.studyPlannerDataStore.data.first()
        val subjectId = prefs[Keys.SW_SUBJECT_ID]?.takeIf { it > 0L }
        val todoId = prefs[Keys.SW_TODO_ID]?.takeIf { it > 0L }
        return PersistedStopwatchState(
            isRunning = prefs[Keys.SW_RUNNING] ?: false,
            accumulatedMillis = prefs[Keys.SW_ACCUMULATED_MS] ?: 0L,
            startWallTimeMillis = prefs[Keys.SW_START_WALL_MS] ?: 0L,
            subjectId = subjectId,
            todoId = todoId
        )
    }

    suspend fun saveStopwatchState(state: PersistedStopwatchState) {
        context.studyPlannerDataStore.edit { prefs ->
            prefs[Keys.SW_RUNNING] = state.isRunning
            prefs[Keys.SW_ACCUMULATED_MS] = state.accumulatedMillis
            prefs[Keys.SW_START_WALL_MS] = state.startWallTimeMillis
            if (state.subjectId != null) {
                prefs[Keys.SW_SUBJECT_ID] = state.subjectId
            } else {
                prefs.remove(Keys.SW_SUBJECT_ID)
            }
            if (state.todoId != null) {
                prefs[Keys.SW_TODO_ID] = state.todoId
            } else {
                prefs.remove(Keys.SW_TODO_ID)
            }
        }
    }

    suspend fun clearStopwatchState() {
        context.studyPlannerDataStore.edit { prefs ->
            prefs[Keys.SW_RUNNING] = false
            prefs[Keys.SW_ACCUMULATED_MS] = 0L
            prefs[Keys.SW_START_WALL_MS] = 0L
            prefs.remove(Keys.SW_SUBJECT_ID)
            prefs.remove(Keys.SW_TODO_ID)
        }
    }
}
