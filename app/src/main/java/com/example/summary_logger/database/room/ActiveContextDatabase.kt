package com.example.summary_logger.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.summary_logger.model.ActiveContext


@Database(entities = [ActiveContext::class], version = 1)
abstract class ActiveContextDatabase : RoomDatabase() {

    abstract fun activeContextDao(): ActiveContextDao

    companion object {
        @Volatile
        private var INSTANCE: ActiveContextDatabase? = null

        fun getInstance(context: Context): ActiveContextDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, ActiveContextDatabase::class.java, "active_context_database")
            .build()
    }
}