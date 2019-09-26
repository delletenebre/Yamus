package kg.delletenebre.yamus.api.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity (
        @PrimaryKey(autoGenerate = false)
        var id: String,

        @ColumnInfo(name = "data")
        var data: String,

        @ColumnInfo(name = "updated_at")
        var updatedAt: Long = System.currentTimeMillis()
)