package com.example.summary_logger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "context_table")
data class Contexts (
    var time: Long = System.currentTimeMillis(),
    var ringerMode: String = "Unknown",
    var batteryLevel: Int = -1,
    var batteryCharging: Boolean = false,
    var isDeviceIdle: Boolean = false,
    var isInteractive: Boolean = false,
    var isPowerSave: Boolean = false,
    var callState: String = "Idle",
    var usageStats: String = "Unknown",
    var light: Float = 0F,
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var network: String = "Unknown"
)
