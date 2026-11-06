package it.appm.myunical.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.appm.myunical.model.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM evento")
    fun getEvents(): Flow<List<Evento>>
    @Query("DELETE FROM evento")
    fun clear()

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    fun insert(events: List<Evento>)

    @Query("DELETE FROM Evento WHERE month = :month AND year = :year")
    suspend fun deleteEventsByMonthAndYear(month: Int, year: Int)

    @Query(
        """
        SELECT * FROM Evento
        WHERE month = :month AND year = :year
        ORDER BY day ASC, startOffset ASC
        """
    )
    fun getEventsByMonthAndYearOrdered(month: Int, year: Int): Flow<List<Evento>>


}