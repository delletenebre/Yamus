package kg.delletenebre.yamus.api.database.dao

import androidx.room.*
import kg.delletenebre.yamus.api.database.table.FavoriteTracksIdsEntity

@Dao
interface FavoriteTracksIdsDao {
    @Query("SELECT * FROM favorite_tracks_ids LIMIT 1")
    fun getFirst(): FavoriteTracksIdsEntity?

    @Query("SELECT * FROM favorite_tracks_ids WHERE revision=:revision")
    fun findByRevision(revision: String): FavoriteTracksIdsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg favoriteTrackIds: FavoriteTracksIdsEntity)

    @Delete
    fun delete(favoriteTrackIds: FavoriteTracksIdsEntity)

    @Update
    fun update(vararg favoriteTrackIds: FavoriteTracksIdsEntity)
}