package com.example.grainclassifier.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing the classification history of a grain.
 */
@Entity(tableName = "classification_history")
data class ClassificationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val grainName: String,
    val confidencePercentage: Int,
    val timestamp: Long,
    val imagePath: String?
)
