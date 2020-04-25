package com.Roo_Media_.lottowish.room.gamelist

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GameDao {
    @Query("SELECT * FROM game")
    fun getAll(): LiveData<List<Game>>

    @Insert
    fun insert(game: Game)

    @Update
    fun update(game: Game)

    @Delete
    fun delete(game: Game)

    @Query("DELETE FROM game")
    fun deleteAll()
}