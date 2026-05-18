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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: NoteRelation)

    @Delete
    suspend fun deleteRelation(relation: NoteRelation)

    @Query("SELECT notes.* FROM notes INNER JOIN note_relations ON notes.id = note_relations.childId WHERE note_relations.parentId = :parentId ORDER BY notes.modified_at DESC")
    fun getSubnotesFlow(parentId: Int): Flow<List<Note>>

    @Query("SELECT notes.* FROM notes INNER JOIN note_relations ON notes.id = note_relations.childId WHERE note_relations.parentId = :parentId ORDER BY notes.modified_at DESC")
    suspend fun getSubnotes(parentId: Int): List<Note>

    @Query("DELETE FROM note_relations WHERE parentId = :parentId OR childId = :parentId")
    suspend fun deleteRelationsForNote(parentId: Int)

    @Query("SELECT * FROM notebooks ORDER BY modified_at DESC")
    fun getAllNotebooksFlow(): Flow<List<Notebook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(notebook: Notebook): Long

    @Delete
    suspend fun deleteNotebook(notebook: Notebook)

    @Query("SELECT * FROM notes WHERE notebook_id = :notebookId ORDER BY modified_at DESC")
    fun getNotesByNotebookFlow(notebookId: Int): Flow<List<Note>>

    @Query("UPDATE notes SET notebook_id = :notebookId WHERE id = :noteId")
    suspend fun moveNoteToNotebook(noteId: Int, notebookId: Int)
}
