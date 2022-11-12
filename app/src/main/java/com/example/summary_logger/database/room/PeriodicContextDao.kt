package com.example.summary_logger.database.room

import androidx.room.*
import com.example.summary_logger.model.PeriodicContext

@Dao
interface PeriodicContextDao {

    @Query("SELECT periodicContextId FROM periodic_context_table")
    fun getAll(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(periodicContext: PeriodicContext)

    @Query("DELETE FROM periodic_context_table WHERE periodicContextId = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM periodic_context_table")
    fun deleteAll()
}