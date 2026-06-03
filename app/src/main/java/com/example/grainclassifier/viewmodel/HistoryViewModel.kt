package com.example.grainclassifier.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.grainclassifier.data.db.AppDatabase
import com.example.grainclassifier.data.entity.ClassificationHistory
import com.example.grainclassifier.data.repository.HistoryRepository
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing ClassificationHistory UI state.
 * Employs AndroidViewModel to gain safe context reference for Room database initialization.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository
    
    /**
     * Exposes all classification records as LiveData, updating the UI whenever database changes.
     */
    val allHistory: LiveData<List<ClassificationHistory>>

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.historyDao()
        repository = HistoryRepository(dao)
        allHistory = repository.allHistory.asLiveData()
    }

    /**
     * Inserts a record. Runs asynchronously on a background thread.
     */
    fun insert(history: ClassificationHistory) = viewModelScope.launch {
        repository.insert(history)
    }

    /**
     * Deletes a specific record. Runs asynchronously on a background thread.
     */
    fun delete(history: ClassificationHistory) = viewModelScope.launch {
        repository.delete(history)
    }

    /**
     * Deletes all history records. Runs asynchronously on a background thread.
     */
    fun clearAll() = viewModelScope.launch {
        repository.clearAll()
    }
}
