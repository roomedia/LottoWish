package com.roomedia.lottowish.room.gamelist

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "game")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val number: String
) : Parcelable
