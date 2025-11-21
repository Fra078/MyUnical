package it.appm.myunical.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Evento(
    @PrimaryKey val id: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val year: Int,
    @ColumnInfo val month: Int,
    @ColumnInfo val day: Int,
    @ColumnInfo val startOffset: Int,
    @ColumnInfo val endOffset: Int,
    @ColumnInfo val location: String,
)
