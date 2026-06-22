package com.mobiapps.quickpdfhub.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecentFileEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Adds nullable outputUri column. Existing rows get NULL, handled by RecentFileEntity.outputUri: String?.
                db.execSQL("ALTER TABLE recent_files ADD COLUMN outputUri TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recent_files_timestamp ON recent_files(timestamp)")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quickpdf_hub.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
