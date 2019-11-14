package kg.delletenebre.yamus.api.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.database.dao.HttpCacheDao
import kg.delletenebre.yamus.api.database.dao.TrackDao
import kg.delletenebre.yamus.api.database.dao.UserTracksIdsDao
import kg.delletenebre.yamus.api.database.table.HttpCacheEntity
import kg.delletenebre.yamus.api.database.table.TrackEntity
import kg.delletenebre.yamus.api.database.table.UserTracksIdsEntity

@Database(
    entities = [TrackEntity::class, UserTracksIdsEntity::class, HttpCacheEntity::class],
    version = 9
)
abstract class YandexDatabase : RoomDatabase(){
    abstract fun trackDao(): TrackDao
    abstract fun userTracksIds(): UserTracksIdsDao
    abstract fun httpCache(): HttpCacheDao

    companion object {
        @Volatile private var instance: YandexDatabase? = null
        private val LOCK = Any()

        operator fun invoke()= instance ?: synchronized(LOCK) {
            instance ?: buildDatabase().also { instance = it}
        }

        private fun buildDatabase() =
                Room.databaseBuilder(
                    App.instance.applicationContext,
                    YandexDatabase::class.java,
                    "yamus-yandex-music.db"
                )
                .fallbackToDestructiveMigration()
                .build()
    }
}