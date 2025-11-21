package it.appm.myunical.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.appm.myunical.model.InfoAppelloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InfoAppelloDao {

    @Query("SELECT * FROM InfoAppelloEntity")
    fun getAll(): Flow<List<InfoAppelloEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(infoAppello: InfoAppelloEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(infoAppello: List<InfoAppelloEntity>)

    @Query("DELETE FROM InfoAppelloEntity")
    suspend fun deleteAll()

}