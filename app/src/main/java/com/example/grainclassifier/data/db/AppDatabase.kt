package com.example.grainclassifier.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.grainclassifier.data.dao.HistoryDao
import com.example.grainclassifier.data.entity.ClassificationHistory

/**
 * The Room Database configuration for the application.
 * Employs a Singleton pattern to prevent multiple database instances.
 */
@Database(entities = [ClassificationHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the Singleton instance of AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grain_classifier_database"
                )
                // Fallback to destructive migration is safe for classification history if required in early dev
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
