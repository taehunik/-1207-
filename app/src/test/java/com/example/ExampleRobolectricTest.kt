package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DDayEntity
import com.example.data.local.StudyPlannerDatabase
import com.example.data.local.StudySessionEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.TodoEntity
import com.example.util.StudyDate
import com.example.util.TimeFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: StudyPlannerDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StudyPlannerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("스터디노트", appName)
    }

    @Test
    fun `studyDate calculates D-Day badges accurately`() {
        val base = StudyDate(2026, 11, 21)
        assertEquals(0, base.daysUntil(StudyDate(2026, 11, 21)))
        assertEquals(10, base.daysUntil(StudyDate(2026, 12, 1)))
        assertEquals(-3, base.daysUntil(StudyDate(2026, 11, 18)))

        assertEquals("D-DAY", TimeFormatter.formatDDayBadge(0))
        assertEquals("D-10", TimeFormatter.formatDDayBadge(10))
        assertEquals("D+3", TimeFormatter.formatDDayBadge(-3))
    }

    @Test
    fun `room dao handles subjects todos sessions featured dday and full reset`() = runTest {
        val dao = database.studyPlannerDao()

        val mathId = dao.insertSubject(SubjectEntity(name = "수학", colorHex = 0xFF3B5BDBL))
        val todoId = dao.insertTodo(
            TodoEntity(title = "미적분 문제집", subjectId = mathId, dateIso = "2026-11-21")
        )
        dao.insertSession(
            StudySessionEntity(
                subjectId = mathId,
                todoId = todoId,
                dateIso = "2026-11-21",
                durationSeconds = 1800L
            )
        )

        val dday1 = dao.insertDDayEnsuringFeatured(
            DDayEntity(title = "중간고사", targetDateIso = "2026-12-01", isFeatured = false, colorHex = 0xFFF76707L)
        )
        val dday2 = dao.insertDDayEnsuringFeatured(
            DDayEntity(title = "기말고사", targetDateIso = "2026-12-20", isFeatured = true, colorHex = 0xFF0CA678L)
        )

        val ddays = dao.observeDDays().first()
        assertEquals(2, ddays.size)
        assertEquals(dday2, ddays.first { it.isFeatured }.id)
        assertEquals(1800L, dao.observeAllSessions().first().sumOf { it.durationSeconds })

        dao.setFeaturedDDay(dday1)
        val updatedDDays = dao.observeDDays().first()
        assertEquals(dday1, updatedDDays.first { it.isFeatured }.id)

        dao.clearAllData()
        assertTrue(dao.observeSubjects().first().isEmpty())
        assertTrue(dao.observeAllTodos().first().isEmpty())
        assertTrue(dao.observeAllSessions().first().isEmpty())
        assertTrue(dao.observeDDays().first().isEmpty())
    }
}
