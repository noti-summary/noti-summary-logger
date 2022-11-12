package com.example.summary_logger.database.room

import androidx.room.*
import com.example.summary_logger.model.ActiveContext

@Dao
interface ActiveContextDao {

    @Query("SELECT activeContextId FROM active_context_table")
    fun getAll(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(activeContext: ActiveContext)

    @Query("DELETE FROM active_context_table WHERE activeContextId = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM active_context_table")
    fun deleteAll()
}