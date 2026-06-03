package com.example.grainclassifier.data.dao

import androidx.room.*
import com.example.grainclassifier.data.entity.ClassificationHistory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) providing database query methods for ClassificationHistory.
 */
@Dao
interface HistoryDao {

    /**
     * Retrieves all history records from the database, sorted newest first.
     * Observed using Kotlin Flow for reactive database queries.
     */
    @Query("SELECT * FROM classification_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ClassificationHistory>>

    /**
     * Inserts a new classification record. If it already exists, replace it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ClassificationHistory)

    /**
     * Deletes a specific classification record.
     */
    @Delete
    suspend fun delete(history: ClassificationHistory)

    /**
     * Clears all classification records from the history.
     */
    @Query("DELETE FROM classification_history")
    suspend fun clearAll()
}
