package com.roomedia.lottowish.room.wishlist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WishDao {
    @Query("SELECT * FROM wish ORDER BY priority")
    fun getAll(): LiveData<List<Wish>>

    @Insert
    fun insert(wish: Wish)

    @Update
    fun update(wish: Wish)

    @Delete
    fun delete(wish: Wish)

    @Query("DELETE FROM wish")
    fun deleteAll()
}
