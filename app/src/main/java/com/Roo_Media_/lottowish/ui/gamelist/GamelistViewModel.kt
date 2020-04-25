package com.Roo_Media_.lottowish.ui.gamelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.Roo_Media_.lottowish.room.AppDatabase
import com.Roo_Media_.lottowish.room.gamelist.Game
import com.Roo_Media_.lottowish.room.gamelist.GameDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GamelistViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao: GameDao by lazy {
        val db = AppDatabase.getInstance(application)!!
        db.gameDao()
    }

    private val games: LiveData<List<Game>> by lazy {
        gameDao.getAll()
    }

    fun getAll(): LiveData<List<Game>> {
        return games
    }

    fun insert(game: Game): Unit {
        CoroutineScope(Dispatchers.IO).launch {
            gameDao.insert(game)
        }
    }

    fun update(game: Game) {
        CoroutineScope(Dispatchers.Default).launch {
            gameDao.update(game)
        }
    }

    fun delete(game: Game): Unit {
        CoroutineScope(Dispatchers.IO).launch {
            gameDao.delete(game)
        }
    }

    fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            gameDao.deleteAll()
        }
    }
}