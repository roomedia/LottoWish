package com.Roo_Media_.lottowish.room.wishlist

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

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
