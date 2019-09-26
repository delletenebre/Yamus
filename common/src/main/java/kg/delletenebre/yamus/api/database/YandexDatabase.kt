package kg.delletenebre.yamus.api.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kg.delletenebre.yamus.api.database.dao.FavoriteTracksIdsDao
import kg.delletenebre.yamus.api.database.dao.TrackDao
import kg.delletenebre.yamus.api.database.table.FavoriteTracksIdsEntity
import kg.delletenebre.yamus.api.database.table.TrackEntity

@Database(
    entities = [TrackEntity::class, FavoriteTracksIdsEntity::class],
    version = 3
)
abstract class YandexDatabase : RoomDatabase(){
    abstract fun trackDao(): TrackDao
    abstract fun favoriteTracksIdsDao(): FavoriteTracksIdsDao

    companion object {
        @Volatile private var instance: YandexDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(
                    context,
                    YandexDatabase::class.java,
                    "yamus-yandex-music.db"
                )
                .fallbackToDestructiveMigration()
                .build()
    }
}