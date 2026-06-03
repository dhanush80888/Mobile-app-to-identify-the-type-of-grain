package com.example.grainclassifier.data.repository

import com.example.grainclassifier.data.dao.HistoryDao
import com.example.grainclassifier.data.entity.ClassificationHistory
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that abstracts access to the database data source.
 * Mediates between domain/viewmodel logic and the Room DAO.
 */
class HistoryRepository(private val historyDao: HistoryDao) {

    /**
     * Observable list of all classification records, sorted newest first.
     */
    val allHistory: Flow<List<ClassificationHistory>> = historyDao.getAllHistory()

    /**
     * Inserts a record.
     */
    suspend fun insert(history: ClassificationHistory) {
        historyDao.insert(history)
    }

    /**
     * Deletes a specific record.
     */
    suspend fun delete(history: ClassificationHistory) {
        historyDao.delete(history)
    }

    /**
     * Deletes all records from the database.
     */
    suspend fun clearAll() {
        historyDao.clearAll()
    }
}
