package com.example.summary_logger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_drawer_table")
data class CurrentDrawer(
    @PrimaryKey(autoGenerate = true)
    var primary_key: Int = 0,
    var notification_id: String
)