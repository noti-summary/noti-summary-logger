package com.example.summary_logger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "periodic_context_table")
data class PeriodicContext (
    @PrimaryKey(autoGenerate = true)
    var periodicContextId: Int = 0,

    var time: Long = System.currentTimeMillis(),
    var batteryLevel: Int = -1,
    var light: Float = 0F,
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var network: String = "Unknown"
)
