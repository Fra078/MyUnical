package it.appm.myunical.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class News(
    @PrimaryKey val id: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val image: String,
    @ColumnInfo val date: Long,
    @ColumnInfo val url: String
)
