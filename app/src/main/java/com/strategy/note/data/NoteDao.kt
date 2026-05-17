package com.strategy.note.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY modified_at DESC")
    fun getAllNotesFlow(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY modified_at DESC LIMIT 1")
    suspend fun getLatestNote(): Note?

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM checklist_items WHERE note_id = :noteId ORDER BY position ASC")
    fun getChecklistItemsFlow(noteId: Int): Flow<List<ChecklistItem>>

    @Query("SELECT * FROM checklist_items WHERE note_id = :noteId ORDER BY position ASC")
    suspend fun getChecklistItems(noteId: Int): List<ChecklistItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItem>)

    @Query("DELETE FROM checklist_items WHERE note_id = :noteId")
    suspend fun deleteChecklistItemsForNote(noteId: Int)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItem)
}
