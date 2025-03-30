package com.roomedia.lottowish.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.roomedia.lottowish.room.gamelist.Game
import com.roomedia.lottowish.room.gamelist.GameDao
import com.roomedia.lottowish.room.picture.Picture
import com.roomedia.lottowish.room.wishlist.Wish
import com.roomedia.lottowish.room.wishlist.WishDao

@Database(entities = [Wish::class, Game::class, Picture::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wishDao(): WishDao
    abstract fun gameDao(): GameDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(AppDatabase::class) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "db.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
