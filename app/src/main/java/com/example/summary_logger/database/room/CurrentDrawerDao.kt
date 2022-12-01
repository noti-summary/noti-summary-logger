package com.example.summary_logger.database.room

import androidx.room.*
import com.example.summary_logger.model.CurrentDrawer

@Dao
interface CurrentDrawerDao {

    @Query("SELECT notificationId FROM current_drawer_table")
    fun getAll(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currentDrawer: CurrentDrawer)

    @Query("DELETE FROM current_drawer_table WHERE notificationId = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM current_drawer_table WHERE packageName = :pkgName AND groupKey = :key")
    fun deleteByPackageKey(pkgName: String, key: String)

    @Query("SELECT * FROM current_drawer_table WHERE packageName = :pkgName AND groupKey = :key")
    fun getByPackageKey(pkgName:String, key: String): List<CurrentDrawer>

    @Query("DELETE FROM current_drawer_table")
    fun deleteAll()
}