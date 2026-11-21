package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyPlannerDao {

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY createdAt ASC")
    fun observeSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    // Todos
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, createdAt DESC")
    fun observeAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE dateIso = :dateIso ORDER BY isCompleted ASC, createdAt DESC")
    fun observeTodosByDate(dateIso: String): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Query("UPDATE todos SET isCompleted = :completed WHERE id = :todoId")
    suspend fun setTodoCompleted(todoId: Long, completed: Boolean)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY createdAt DESC")
    fun observeAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateIso = :dateIso ORDER BY createdAt DESC")
    fun observeSessionsByDate(dateIso: String): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    // D-Days
    @Query("SELECT * FROM ddays ORDER BY isFeatured DESC, targetDateIso ASC")
    fun observeDDays(): Flow<List<DDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDDay(dDay: DDayEntity): Long

    @Update
    suspend fun updateDDay(dDay: DDayEntity)

    @Delete
    suspend fun deleteDDay(dDay: DDayEntity)

    @Query("UPDATE ddays SET isFeatured = 0")
    suspend fun clearFeaturedDDay()

    @Query("UPDATE ddays SET isFeatured = 1 WHERE id = :ddayId")
    suspend fun markFeaturedDDay(ddayId: Long)

    @Query("SELECT COUNT(*) FROM ddays WHERE isFeatured = 1")
    suspend fun countFeaturedDDays(): Int

    @Query("SELECT id FROM ddays ORDER BY targetDateIso ASC LIMIT 1")
    suspend fun getFirstDDayId(): Long?

    @Transaction
    suspend fun setFeaturedDDay(ddayId: Long) {
        clearFeaturedDDay()
        markFeaturedDDay(ddayId)
    }

    @Transaction
    suspend fun insertDDayEnsuringFeatured(dDay: DDayEntity): Long {
        if (dDay.isFeatured) {
            clearFeaturedDDay()
        }
        val insertedId = insertDDay(dDay)
        if (countFeaturedDDays() == 0) {
            markFeaturedDDay(insertedId)
        }
        return insertedId
    }

    @Transaction
    suspend fun deleteDDayAndReassignFeatured(dDay: DDayEntity) {
        deleteDDay(dDay)
        if (dDay.isFeatured) {
            val nextId = getFirstDDayId()
            if (nextId != null) {
                markFeaturedDDay(nextId)
            }
        }
    }

    @Transaction
    suspend fun clearAllData() {
        deleteAllSessions()
        deleteAllTodos()
        deleteAllDDays()
        deleteAllSubjects()
    }

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()

    @Query("DELETE FROM ddays")
    suspend fun deleteAllDDays()

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()
}
