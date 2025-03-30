package com.roomedia.lottowish.ui.wishlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.roomedia.lottowish.room.AppDatabase
import com.roomedia.lottowish.room.wishlist.Wish
import com.roomedia.lottowish.room.wishlist.WishDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WishlistViewModel(application: Application) : AndroidViewModel(application) {

    private val wishDao: WishDao by lazy {
        val db = AppDatabase.getInstance(application)
        db.wishDao()
    }

    private val wishes: LiveData<List<Wish>> by lazy {
        wishDao.getAll()
    }

    fun getAll(): LiveData<List<Wish>> {
        return wishes
    }

    fun insert(wish: Wish) {
        CoroutineScope(Dispatchers.Default).launch {
            wishDao.insert(wish)
        }
    }

    fun update(wish: Wish) {
        CoroutineScope(Dispatchers.Default).launch {
            wishDao.update(wish)
        }
    }

    fun delete(wish: Wish) {
        CoroutineScope(Dispatchers.Default).launch {
            wishDao.delete(wish)
        }
    }

    fun deleteAll() {
        CoroutineScope(Dispatchers.Default).launch {
            wishDao.deleteAll()
        }
    }
}
