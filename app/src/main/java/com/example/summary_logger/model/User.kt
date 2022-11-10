package com.example.summary_logger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey
    var primaryKey: Int = 1,
    var userId: String
)