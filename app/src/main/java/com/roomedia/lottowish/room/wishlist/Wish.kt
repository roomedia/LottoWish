package com.roomedia.lottowish.room.wishlist

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "wish")
data class Wish(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val cost: Long,
    @ColumnInfo(index = true)
    var priority: Int
) : Parcelable
