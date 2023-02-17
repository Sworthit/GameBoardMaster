package com.mrawluk.gameboardmaster.models

import android.os.Parcel
import android.os.Parcelable

data class GameEvent(
    val name: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val eventDate: Long = 0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readLong()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int)= with(parcel) {
        parcel.writeString(name)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeLong(eventDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GameEvent> {
        override fun createFromParcel(parcel: Parcel): GameEvent {
            return GameEvent(parcel)
        }

        override fun newArray(size: Int): Array<GameEvent?> {
            return arrayOfNulls(size)
        }
    }
}
