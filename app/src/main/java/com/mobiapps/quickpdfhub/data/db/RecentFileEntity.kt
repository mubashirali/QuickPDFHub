package com.mobiapps.quickpdfhub.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val operation: String,
    val size: String,
    // Index speeds up trimOld() ORDER BY timestamp DESC queries.
    @ColumnInfo(index = true) val timestamp: Long,
    val outputUri: String? = null,
)
