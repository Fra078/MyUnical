package it.appm.myunical.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibrettoDao {
    @Query("SELECT * FROM librettoitem")
    fun getAll(): Flow<List<LibrettoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg discipline: LibrettoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(discipline: List<LibrettoItem>)

    @Query("DELETE FROM librettoitem")
    fun deleteAll()

}