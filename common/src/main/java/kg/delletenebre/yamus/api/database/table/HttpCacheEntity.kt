package kg.delletenebre.yamus.api.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "http_cache")
data class HttpCacheEntity (
        @PrimaryKey(autoGenerate = false)
        var url: String = "",

        @ColumnInfo(name = "response")
        var response: String = "",

        @ColumnInfo(name = "updated_at")
        var updatedAt: Long = 0
) {
        fun isFresh(): Boolean {
            return System.currentTimeMillis() - updatedAt > 1000 * 60 // 1 min
        }
}