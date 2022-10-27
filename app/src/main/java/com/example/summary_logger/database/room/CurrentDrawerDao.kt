package com.example.summary_logger.database.room

import androidx.room.*
import com.example.summary_logger.model.CurrentDrawer

@Dao
interface CurrentDrawerDao {

    @Query("SELECT notification_id FROM current_drawer_table")
    fun getAll(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currentDrawer: CurrentDrawer)

    @Query("DELETE FROM current_drawer_table WHERE notification_id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM current_drawer_table")
    fun deleteAll()
}