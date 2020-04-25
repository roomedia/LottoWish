package com.Roo_Media_.lottowish.room.picture

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PictureDao {
    @Query("SELECT * FROM picture WHERE wishId=:wishId")
    fun getAllByFk(wishId: Int): LiveData<List<Picture>>

    @Insert
    fun insert(picture: Picture)

    @Update
    fun update(picture: Picture)

    @Delete
    fun delete(picture: Picture)

    @Query("DELETE FROM picture")
    fun deleteAll()
}