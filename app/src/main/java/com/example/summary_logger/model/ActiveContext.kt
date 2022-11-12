package com.example.summary_logger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "context_table")
data class ActiveContext (
    @PrimaryKey
    var primaryKey: Int = 1,

    var time: Long = System.currentTimeMillis(),
    var ringerMode: String = "Unknown",
    var batteryCharging: Boolean = false,
    var isDeviceIdle: Boolean = false,
    var isInteractive: Boolean = false,
    var isPowerSave: Boolean = false,
    var callState: String = "Idle",
    var usageStats: String = "Unknown",
)
