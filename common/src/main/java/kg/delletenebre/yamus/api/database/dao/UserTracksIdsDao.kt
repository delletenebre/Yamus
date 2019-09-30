package kg.delletenebre.yamus.api.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kg.delletenebre.yamus.api.database.table.UserTracksIdsEntity

@Dao
interface UserTracksIdsDao {
    @Query("SELECT * FROM user_tracks_ids WHERE type=:type")
    fun get(type: String): UserTracksIdsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg userTrackIds: UserTracksIdsEntity)
}