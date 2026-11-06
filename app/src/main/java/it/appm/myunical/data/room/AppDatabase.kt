package it.appm.myunical.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import it.appm.myunical.model.Evento

@Database(entities = [Evento::class], version = 12)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getScheduleDao(): ScheduleDao
}