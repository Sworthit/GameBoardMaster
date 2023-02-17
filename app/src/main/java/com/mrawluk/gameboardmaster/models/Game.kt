package com.mrawluk.gameboardmaster.models

import android.os.Parcel
import android.os.Parcelable

data class Game(
    var title: String = "",
    val createdBy: String = "",
    var gameEvents: ArrayList<GameEvent> = ArrayList()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(GameEvent.CREATOR)!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, p1: Int) = with(dest) {
        writeString(title)
        writeString(createdBy)
        writeTypedList(gameEvents)
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }
    }

}