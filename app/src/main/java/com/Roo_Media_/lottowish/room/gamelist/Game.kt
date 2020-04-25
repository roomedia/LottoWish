package com.Roo_Media_.lottowish.room.gamelist

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "game")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val number: String
) : Parcelable