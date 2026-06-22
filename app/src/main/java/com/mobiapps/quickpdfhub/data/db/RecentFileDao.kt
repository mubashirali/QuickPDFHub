package com.mobiapps.quickpdfhub.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecentFileDao {
    @Insert
    suspend fun insert(file: RecentFileEntity)

    @Query("SELECT * FROM recent_files ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecent(): List<RecentFileEntity>

    @Query("DELETE FROM recent_files WHERE id NOT IN (SELECT id FROM recent_files ORDER BY timestamp DESC LIMIT 50)")
    suspend fun trimOld()
}
