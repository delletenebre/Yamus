package kg.delletenebre.yamus.api.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kg.delletenebre.yamus.api.database.dao.TrackDao
import kg.delletenebre.yamus.api.database.dao.UserTracksIdsDao
import kg.delletenebre.yamus.api.database.table.TrackEntity
import kg.delletenebre.yamus.api.database.table.UserTracksIdsEntity

@Database(
    entities = [TrackEntity::class, UserTracksIdsEntity::class],
    version = 4
)
abstract class YandexDatabase : RoomDatabase(){
    abstract fun trackDao(): TrackDao
    abstract fun userTracksIds(): UserTracksIdsDao

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