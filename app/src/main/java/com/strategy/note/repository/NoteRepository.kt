package com.strategy.note.repository

import com.strategy.note.data.Note
import com.strategy.note.data.ChecklistItem
import com.strategy.note.data.NoteDao
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
        noteDao.deleteNote(note)
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
}
