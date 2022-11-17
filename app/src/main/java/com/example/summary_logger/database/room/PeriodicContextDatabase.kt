package com.example.summary_logger.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.summary_logger.model.PeriodicContext


@Database(entities = [PeriodicContext::class], version = 1)
abstract class PeriodicContextDatabase : RoomDatabase() {

    abstract fun periodicContextDao(): PeriodicContextDao

    companion object {
        @Volatile
        private var INSTANCE: PeriodicContextDatabase? = null

        fun getInstance(context: Context): PeriodicContextDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, PeriodicContextDatabase::class.java, "periodic_context_database")
            .build()
    }
}