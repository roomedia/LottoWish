package com.Roo_Media_.lottowish.ui.wish

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.Roo_Media_.lottowish.room.AppDatabase
import com.Roo_Media_.lottowish.room.picture.Picture
import com.Roo_Media_.lottowish.room.picture.PictureDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PictureViewModel(application: Application) : AndroidViewModel(application) {

    private val pictureDao: PictureDao by lazy {
        val db = AppDatabase.getInstance(application)!!
        db.pictureDao()
    }

    fun getAll(fk: Int): LiveData<List<Picture>> {
        return pictureDao.getAllByFk(fk)
    }

    fun insert(picture: Picture) {
        CoroutineScope(Dispatchers.Default).launch {
            pictureDao.insert(picture)
        }
    }

    fun update(picture: Picture) {
        CoroutineScope(Dispatchers.Default).launch {
            pictureDao.update(picture)
        }
    }

    fun delete(picture: Picture) {
        CoroutineScope(Dispatchers.Default).launch {
            pictureDao.delete(picture)
        }
    }

    fun deleteAll() {
        CoroutineScope(Dispatchers.Default).launch {
            pictureDao.deleteAll()
        }
    }
}