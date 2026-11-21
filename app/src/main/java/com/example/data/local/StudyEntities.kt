package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val colorHex: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["dateIso"])
    ]
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val subjectId: Long? = null,
    val dateIso: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = TodoEntity::class,
            parentColumns = ["id"],
            childColumns = ["todoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["todoId"]),
        Index(value = ["dateIso"])
    ]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subjectId: Long? = null,
    val todoId: Long? = null,
    val dateIso: String,
    val durationSeconds: Long,
    val memo: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ddays")
data class DDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val targetDateIso: String,
    val isFeatured: Boolean = false,
    val colorHex: Long,
    val createdAt: Long = System.currentTimeMillis()
)
