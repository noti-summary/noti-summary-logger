package com.example.summary_logger.database

import androidx.room.*
import com.example.summary_logger.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table")
    fun getAllUser(): List<User>

    @Query("SELECT user_id FROM user_table WHERE primary_key=1")
    fun getCurrentUserId(): String  // current user's primary_key = 1

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setUser(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)
}