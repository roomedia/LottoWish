package com.Roo_Media_.lottowish.room.wishlist

import androidx.lifecycle.LiveData
import androidx.room.*

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