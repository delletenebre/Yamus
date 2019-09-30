package kg.delletenebre.yamus.api.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_tracks_ids")
data class UserTracksIdsEntity (
        @PrimaryKey(autoGenerate = false)
        var type: String = "",

        var revision: Int = 0,

        @ColumnInfo(name = "tracks_ids")
        var tracksIds: String = "",

        @ColumnInfo(name = "tracks_count")
        var tracksCount: Int = 0,

        @ColumnInfo(name = "duration_ms")
        var duration: Long = 0
)