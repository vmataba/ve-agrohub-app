package com.tabaapps.ve_agrohub_app

import android.app.Application
import com.tabaapps.ve_agrohub_app.data.local.VeStockDatabase
import com.tabaapps.ve_agrohub_app.data.repository.AuthRepository
import com.tabaapps.ve_agrohub_app.data.repository.VeStockRepository
import com.tabaapps.ve_agrohub_app.data.sync.FirestoreSyncEngine

/**
 * Main Application class for ve-stock app ("QuestionAnswersApplication").
 *
 * Java Educational Note:
 * In Android, `android.app.Application` is initialized once when the app process starts.
 * Lazy properties (`by lazy`) in Kotlin are equivalent to thread-safe double-checked lock singletons in Java.
 */
class QuestionAnswersApplication : Application() {

    val database by lazy { VeStockDatabase.getDatabase(this) }
    val repository by lazy { VeStockRepository(database) }
    val authRepository by lazy { AuthRepository(this, database.allowedUserDao()) }
    val syncEngine by lazy { FirestoreSyncEngine(this, database) }
}
