package com.strategy.note.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.strategy.note.data.Note
import com.strategy.note.data.ChecklistItem
import com.strategy.note.data.Notebook
import android.content.SharedPreferences
import com.strategy.note.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import android.content.Context
import com.strategy.note.receiver.AlarmScheduler

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = repository.allNotesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedColorFilter = MutableStateFlow<Long?>(null)
    val selectedColorFilter: StateFlow<Long?> = _selectedColorFilter.asStateFlow()

    val allNotebooks: StateFlow<List<Notebook>> = repository.allNotebooksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNotebookId = MutableStateFlow(0)
    val selectedNotebookId: StateFlow<Int> = _selectedNotebookId.asStateFlow()

    val notesByNotebook: StateFlow<List<Note>> = _selectedNotebookId.flatMapLatest { nbId ->
        if (nbId > 0) repository.getNotesByNotebookFlow(nbId)
        else repository.allNotesFlow
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        val filteredNotes: StateFlow<List<Note>> = combine(notesByNotebook, _searchQuery, _selectedColorFilter) { notes, query, color ->
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

    fun generateDefaultTitle(): String {
        val today = java.util.Calendar.getInstance()
        val day = today.get(java.util.Calendar.DAY_OF_MONTH)
        val month = today.get(java.util.Calendar.MONTH) + 1
        val yearFull = today.get(java.util.Calendar.YEAR)
        val yearTwoDigits = yearFull % 100
        
        val todayNotesCount = allNotes.value.count { note ->
            val noteCalendar = java.util.Calendar.getInstance().apply { timeInMillis = note.createdAt }
            noteCalendar.get(java.util.Calendar.DAY_OF_MONTH) == day &&
            noteCalendar.get(java.util.Calendar.MONTH) + 1 == month &&
            noteCalendar.get(java.util.Calendar.YEAR) == yearFull
        }
        
        val nextNumber = todayNotesCount + 1
        val numberStr = String.format(java.util.Locale.US, "%02d", nextNumber)
        val dateStr = String.format(java.util.Locale.US, "%02d-%02d-%02d", day, month, yearTwoDigits)
        
        return "nota $numberStr $dateStr"
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

    fun getSubnotes(parentId: Int): Flow<List<Note>> {
        return repository.getSubnotesFlow(parentId)
    }

    fun linkSubnote(parentId: Int, childId: Int) {
        viewModelScope.launch {
            repository.insertRelation(parentId, childId)
        }
    }

    fun unlinkSubnote(parentId: Int, childId: Int) {
        viewModelScope.launch {
            repository.deleteRelation(parentId, childId)
        }
    }

    fun selectNotebook(notebookId: Int) {
        _selectedNotebookId.value = notebookId
    }

    fun createNotebook(name: String, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val notebook = Notebook(name = name)
            val id = repository.insertNotebook(notebook)
            onComplete(id)
        }
    }

    fun renameNotebook(notebook: Notebook, newName: String) {
        viewModelScope.launch {
            repository.insertNotebook(notebook.copy(name = newName, modifiedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNotebook(notebook: Notebook) {
        viewModelScope.launch {
            repository.deleteNotebook(notebook)
        }
    }

    fun saveLastNotebookId(prefs: SharedPreferences, notebookId: Int) {
        prefs.edit().putInt("last_notebook_id", notebookId).apply()
    }

    fun getLastNotebookId(prefs: SharedPreferences): Int {
        return prefs.getInt("last_notebook_id", 0)
    }

    fun isAutoOpenLastNotebook(prefs: SharedPreferences): Boolean {
        return prefs.getBoolean("auto_open_last_notebook", true)
    }

    fun setAutoOpenLastNotebook(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean("auto_open_last_notebook", enabled).apply()
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
