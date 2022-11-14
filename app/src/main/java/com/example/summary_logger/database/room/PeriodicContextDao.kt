package com.example.summary_logger.database.room

import androidx.room.*
import com.example.summary_logger.model.PeriodicContext

@Dao
interface PeriodicContextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(periodicContext: PeriodicContext)

    @Query("DELETE FROM periodic_context_table WHERE periodicContextId = :id")
    fun deleteById(id: Int)

    @Query("SELECT * FROM periodic_context_table WHERE time >= :beginTime AND time < :endTime")
    fun queryByTimeInterval(beginTime: Long, endTime: Long): List<PeriodicContext>

    @Query("SELECT * FROM periodic_context_table")
    fun getAll(): List<PeriodicContext>

    @Query("DELETE FROM periodic_context_table")
    fun deleteAll()
}