package com.roomedia.lottowish.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Preferences(private val activity: Activity) {

    private val preferences: SharedPreferences by lazy {
        activity.getPreferences(Context.MODE_PRIVATE)
    }
    private val KEY = "LAST_INDEX_OF_ADD_GAME"

    fun getLatestIndex(): Int {
        return preferences.getInt(KEY, 0)
    }

    fun setLatestIndex(lastIndex: Int) {
        preferences.edit {
            this.putInt(KEY, lastIndex)
        }
    }
}
