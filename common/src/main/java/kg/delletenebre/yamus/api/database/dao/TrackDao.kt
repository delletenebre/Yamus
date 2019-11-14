package kg.delletenebre.yamus.api.database.dao

import androidx.room.*
import kg.delletenebre.yamus.api.database.table.TrackEntity

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks")
    suspend fun getAll(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE id=:id")
    suspend fun findById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<String>): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity)

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(track: TrackEntity)

    @Update
    suspend fun update(track: TrackEntity)

    @Transaction
    suspend fun insert(tracks: List<TrackEntity>) {
        tracks.forEach {
            insert(it)
        }
    }


}