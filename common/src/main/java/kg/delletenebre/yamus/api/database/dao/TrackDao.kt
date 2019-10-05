package kg.delletenebre.yamus.api.database.dao

import androidx.room.*
import kg.delletenebre.yamus.api.database.table.TrackEntity

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks")
    fun getAll(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE id=:id")
    fun findById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    fun findByIds(ids: List<String>): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg track: TrackEntity)

    @Delete
    fun delete(track: TrackEntity)

    @Update
    fun update(vararg track: TrackEntity)

    @Transaction
    fun insert(tracks: List<TrackEntity>) {
        tracks.forEach {
            insert(it)
        }
    }


}