package it.appm.myunical.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import it.appm.myunical.model.Evento
import it.appm.myunical.model.InfoAppelloEntity
import it.appm.myunical.model.News

@Database(entities = [LibrettoItem::class, News::class, Evento::class, InfoAppelloEntity::class], version = 10)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getLibrettoDao(): LibrettoDao
    abstract fun getNewsDao(): NewsDao
    abstract fun getScheduleDao(): ScheduleDao
    abstract fun getInfoAppelloDao(): InfoAppelloDao
}