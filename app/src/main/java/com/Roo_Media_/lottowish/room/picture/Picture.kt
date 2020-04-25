package com.Roo_Media_.lottowish.room.picture

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.Roo_Media_.lottowish.room.wishlist.Wish
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "picture",
    foreignKeys = [ForeignKey(
        entity = Wish::class,
        parentColumns = ["id"],
        childColumns = ["wishId"]
    )]
)
data class Picture(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val wishId: Int,
    val picture: String
) : Parcelable