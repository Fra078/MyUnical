package it.appm.myunical.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LibrettoItem(
    @PrimaryKey val id: Int,
    @ColumnInfo("title") val title : String,
    @ColumnInfo("cfu") val cfu: Int,
    @ColumnInfo("data") val date: String?,
    @ColumnInfo("vote") val vote: Int?,
    @ColumnInfo("laude") val laude: Boolean,
)