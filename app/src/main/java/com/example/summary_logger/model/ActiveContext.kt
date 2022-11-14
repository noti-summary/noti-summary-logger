package com.example.summary_logger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_context_table")
data class ActiveContext (
    @PrimaryKey(autoGenerate = true)
    var activeContextId: Int = 0,

    var time: Long = System.currentTimeMillis(),
    var ringerMode: String = "Unknown",
    var batteryCharging: Boolean = false, // TODO To be tested
    var isDeviceIdle: Boolean = false,
    var isInteractive: Boolean = false,
    var isPowerSave: Boolean = false,
    var callState: String = "Idle", // TODO To be tested
    var usageStats: String = "Unknown",
)
