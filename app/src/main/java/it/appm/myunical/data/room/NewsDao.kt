package it.appm.myunical.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.appm.myunical.model.News
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY date DESC")
    fun getNews(): Flow<List<News>>
    @Query("DELETE FROM news")
    fun clear()

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    fun insert(news: List<News>)

}