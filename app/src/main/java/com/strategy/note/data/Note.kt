package com.strategy.note.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

enum class NoteType(val value: Int) {
    TEXT(0),
    CHECKLIST(1);
    
    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.value == value } ?: TEXT
    }
}

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String = "",
    val type: Int = NoteType.TEXT.value,
    @ColumnInfo(name = "color_code") val colorCode: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "modified_at") val modifiedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "reminder_time") val reminderTime: Long? = null,
    @ColumnInfo(name = "is_locked") val isLocked: Boolean = false,
    @ColumnInfo(name = "notebook_id") val notebookId: Int = 0,
    @ColumnInfo(name = "images") val images: String = ""
)

@Entity(tableName = "checklist_items")
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "note_id") val noteId: Int,
    val text: String,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean = false,
    val position: Int = 0
)

@Entity(
    tableName = "note_relations",
    primaryKeys = ["parentId", "childId"]
)
data class NoteRelation(
    val parentId: Int,
    val childId: Int
)

@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "modified_at") val modifiedAt: Long = System.currentTimeMillis()
)
