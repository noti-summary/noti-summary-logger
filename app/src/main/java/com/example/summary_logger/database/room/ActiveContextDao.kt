package com.example.summary_logger.database.room

import androidx.room.*
import com.example.summary_logger.model.ActiveContext

@Dao
interface ActiveContextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(activeContext: ActiveContext)

    @Query("DELETE FROM active_context_table WHERE activeContextId = :id")
    fun deleteById(id: Int)

    @Query("SELECT * FROM active_context_table WHERE time >= :beginTime AND time < :endTime")
    fun queryByTimeInterval(beginTime: Long, endTime: Long): List<ActiveContext>

    @Query("SELECT * FROM active_context_table")
    fun getAll(): List<ActiveContext>

    @Query("DELETE FROM active_context_table")
    fun deleteAll()
}