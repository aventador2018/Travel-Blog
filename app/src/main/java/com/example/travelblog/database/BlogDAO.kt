package com.example.travelblog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.travelblog.http.Blog

@Dao
interface BlogDAO {

    @Query("SELECT * FROM blog")
    fun getAll(): List<Blog>

    @Insert
    fun insertAll(blogList: List<Blog>)

    @Query("DELETE FROM blog")
    fun deleteAll()
}