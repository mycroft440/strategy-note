package com.strategy.note.repository

import com.strategy.note.data.Note
import com.strategy.note.data.ChecklistItem
import com.strategy.note.data.NoteDao
import com.strategy.note.data.Notebook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {
    val allNotesFlow: Flow<List<Note>> = noteDao.getAllNotesFlow()

    suspend fun getNoteById(id: Int): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)
    }

    suspend fun insertNote(note: Note): Int = withContext(Dispatchers.IO) {
        noteDao.insertNote(note).toInt()
    }

    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteRelationsForNote(note.id)
        noteDao.deleteNote(note)
    }

    fun getSubnotesFlow(parentId: Int): Flow<List<Note>> {
        return noteDao.getSubnotesFlow(parentId)
    }

    suspend fun insertRelation(parentId: Int, childId: Int) = withContext(Dispatchers.IO) {
        noteDao.insertRelation(com.strategy.note.data.NoteRelation(parentId, childId))
    }

    suspend fun deleteRelation(parentId: Int, childId: Int) = withContext(Dispatchers.IO) {
        noteDao.deleteRelation(com.strategy.note.data.NoteRelation(parentId, childId))
    }

    fun getChecklistItemsFlow(noteId: Int): Flow<List<ChecklistItem>> {
        return noteDao.getChecklistItemsFlow(noteId)
    }

    suspend fun getChecklistItems(noteId: Int): List<ChecklistItem> = withContext(Dispatchers.IO) {
        noteDao.getChecklistItems(noteId)
    }

    suspend fun saveChecklistItems(noteId: Int, items: List<ChecklistItem>) = withContext(Dispatchers.IO) {
        noteDao.deleteChecklistItemsForNote(noteId)
        val itemsWithCorrectId = items.map { it.copy(noteId = noteId) }
        noteDao.insertChecklistItems(itemsWithCorrectId)
    }

    val allNotebooksFlow: Flow<List<Notebook>> = noteDao.getAllNotebooksFlow()

    suspend fun insertNotebook(notebook: Notebook): Int = withContext(Dispatchers.IO) {
        noteDao.insertNotebook(notebook).toInt()
    }

    suspend fun deleteNotebook(notebook: Notebook) = withContext(Dispatchers.IO) {
        noteDao.deleteNotebook(notebook)
    }

    fun getNotesByNotebookFlow(notebookId: Int): Flow<List<Note>> {
        return noteDao.getNotesByNotebookFlow(notebookId)
    }

    suspend fun moveNoteToNotebook(noteId: Int, notebookId: Int) = withContext(Dispatchers.IO) {
        noteDao.moveNoteToNotebook(noteId, notebookId)
    }
}
