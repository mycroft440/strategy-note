package com.strategy.note.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.strategy.note.data.Note
import com.strategy.note.data.ChecklistItem
import com.strategy.note.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import com.strategy.note.receiver.AlarmScheduler

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = repository.allNotesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedColorFilter = MutableStateFlow<Long?>(null)
    val selectedColorFilter: StateFlow<Long?> = _selectedColorFilter.asStateFlow()

        val filteredNotes: StateFlow<List<Note>> = combine(allNotes, _searchQuery, _selectedColorFilter) { notes, query, color ->
        val filtered = notes.filter { note -> color == null || note.colorCode == color }
        if (query.isEmpty()) {
            filtered
        } else {
            val titleMatches = filtered.filter { note ->
                note.title.contains(query, ignoreCase = true)
            }
            val contentMatches = filtered.filter { note ->
                note.content.contains(query, ignoreCase = true) && !note.title.contains(query, ignoreCase = true)
            }
            titleMatches + contentMatches
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setColorFilter(color: Long?) {
        _selectedColorFilter.value = color
    }

    fun saveNote(context: Context, note: Note, checklistItems: List<ChecklistItem> = emptyList(), onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val noteId = repository.insertNote(note)
            val finalNote = note.copy(id = noteId)
            if (note.type == 1) {
                repository.saveChecklistItems(noteId, checklistItems)
            }
            if (finalNote.reminderTime != null) {
                AlarmScheduler.scheduleAlarm(context.applicationContext, finalNote)
            } else {
                AlarmScheduler.cancelAlarm(context.applicationContext, noteId)
            }
            onComplete(noteId)
        }
    }

    fun deleteNote(context: Context, note: Note) {
         viewModelScope.launch {
            AlarmScheduler.cancelAlarm(context.applicationContext, note.id)
            repository.deleteNote(note)
        }
    }

    fun getChecklistItems(noteId: Int): Flow<List<ChecklistItem>> {
        return repository.getChecklistItemsFlow(noteId)
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                return NoteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
