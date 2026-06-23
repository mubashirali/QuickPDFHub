package com.mobiapps.quickpdfhub.data

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.mobiapps.quickpdfhub.data.db.AppDatabase
import com.mobiapps.quickpdfhub.data.db.RecentFileEntity
import com.mobiapps.quickpdfhub.domain.WorkResult
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object PdfWorkSession {
    var inputUris: List<Uri> = emptyList()
        private set

    var compressionQuality: Int = 35

    var lastResult: WorkResult.Success? = null
    var lastErrorMessage: String? = null

    var outputUri: Uri? = null

    // Page selection state for SPLIT / DELETE / REORDER tools
    var splitAfterPage: Int = 1
    var pagesToDelete: List<Int> = emptyList()
    var reorderedPageIndices: List<Int> = emptyList()

    val recentEntries: SnapshotStateList<RecentFile> = mutableStateListOf()
    private val loadMutex = Mutex()
    @Volatile private var roomLoaded = false

    fun addRecent(entry: RecentFile) {
        recentEntries.add(0, entry)
        if (recentEntries.size > 50) recentEntries.removeAt(recentEntries.lastIndex)
    }

    suspend fun addRecentToRoom(context: Context, entry: RecentFile, timestamp: Long = System.currentTimeMillis()) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            db.recentFileDao().insert(
                RecentFileEntity(
                    name = entry.name,
                    operation = entry.operation,
                    size = entry.size,
                    timestamp = timestamp,
                    outputUri = entry.outputUri,
                )
            )
            db.recentFileDao().trimOld()
        }
    }

    suspend fun loadFromRoom(context: Context) {
        if (roomLoaded) return  // fast path without acquiring lock
        loadMutex.withLock {
            if (roomLoaded) return  // re-check inside lock
            try {
                val entities = AppDatabase.getInstance(context).recentFileDao().getRecent()
                val entries = entities.map { e ->
                    RecentFile(
                        name = e.name,
                        operation = e.operation,
                        size = e.size,
                        date = formatTimestamp(e.timestamp),
                        outputUri = e.outputUri,
                    )
                }
                recentEntries.addAll(entries)
                roomLoaded = true  // only set after successful load
            } catch (e: Exception) {
                android.util.Log.e("PdfWorkSession", "loadFromRoom failed", e)
            }
        }
    }

    fun setInputs(uris: List<Uri>) {
        inputUris = uris
        outputUri = null
        lastResult = null
        splitAfterPage = 1
        pagesToDelete = emptyList()
        reorderedPageIndices = emptyList()
    }

    fun clear() {
        inputUris = emptyList()
        outputUri = null
        lastResult = null
        compressionQuality = 35
        splitAfterPage = 1
        pagesToDelete = emptyList()
        reorderedPageIndices = emptyList()
    }

    val primaryInput: Uri? get() = inputUris.firstOrNull()
    val hasInput: Boolean get() = inputUris.isNotEmpty()

    internal fun formatTimestamp(timestamp: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = timestamp }
        return when {
            isSameDay(now, then) -> "Today"
            isYesterday(now, then) -> "Yesterday"
            else -> {
                val month = then.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
                val day = then.get(Calendar.DAY_OF_MONTH)
                "$month $day"
            }
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(now: Calendar, then: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, then)
    }
}
