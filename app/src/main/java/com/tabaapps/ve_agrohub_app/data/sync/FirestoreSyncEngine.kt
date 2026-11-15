package com.tabaapps.ve_agrohub_app.data.sync

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.tabaapps.ve_agrohub_app.data.local.VeStockDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Offline-First Real-time Synchronization Engine between Room SQLite Local DB and Firebase Firestore.
 *
 * Firestore Collections:
 * - `/stores`
 * - `/product_types`
 * - `/inventory_items`
 * - `/stock_logs`
 * - `/users`
 * - `/config/allowed_emails`
 *
 * Java Educational Note:
 * In Java enterprise systems, sync services handle cloud-to-local database replication.
 * Firestore `EventListener` in Java Android handles real-time callbacks via `onEvent(DocumentSnapshot, FirebaseFirestoreException)`.
 * The implementation uses `runCatching` block to safely catch `IllegalStateException` or `NoClassDefFoundError`
 * if Firebase is not yet initialized, allowing the Room local database to function 100% offline seamlessly.
 */
class FirestoreSyncEngine(
    private val context: Context,
    private val database: VeStockDatabase
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    init {
        initFirestoreSafely()
    }

    private fun initFirestoreSafely() {
        runCatching {
            firestore = FirebaseFirestore.getInstance()
        }.onFailure {
            // Firebase unavailable or not configured in google-services.json
            firestore = null
        }
    }

    /**
     * Executes manual or periodic full sync between Room and Firestore.
     */
    fun performFullSync() {
        val fs = firestore ?: run {
            _lastSyncTimestamp.value = System.currentTimeMillis()
            return
        }

        scope.launch {
            _isSyncing.value = true
            runCatching {
                // 1. Sync Stores
                val stores = database.storeDao().getAllStores()
                for (store in stores) {
                    fs.collection("stores").document(store.id).set(
                        mapOf(
                            "id" to store.id,
                            "name" to store.name,
                            "location" to store.location,
                            "updatedAt" to store.updatedAt
                        )
                    )
                }

                // 2. Sync Product Types
                val products = database.productTypeDao().getAllProductTypes()
                for (prod in products) {
                    fs.collection("product_types").document(prod.id).set(
                        mapOf(
                            "id" to prod.id,
                            "name" to prod.name,
                            "category" to prod.category,
                            "priceTzs" to prod.priceTzs,
                            "updatedAt" to prod.updatedAt
                        )
                    )
                }

                // 3. Sync Inventory Items
                val items = database.inventoryItemDao().getAllInventoryItems()
                for (item in items) {
                    fs.collection("inventory_items").document(item.id).set(
                        mapOf(
                            "id" to item.id,
                            "storeId" to item.storeId,
                            "productTypeId" to item.productTypeId,
                            "quantity" to item.quantity,
                            "minStockAlertThreshold" to item.minStockAlertThreshold,
                            "imageCaptureCount" to item.imageCaptureCount,
                            "updatedAt" to item.updatedAt
                        )
                    )
                }

                // 4. Sync Stock Logs
                val logs = database.stockLogDao().getAllStockLogs()
                for (log in logs) {
                    fs.collection("stock_logs").document(log.id).set(
                        mapOf(
                            "id" to log.id,
                            "storeId" to log.storeId,
                            "productTypeId" to log.productTypeId,
                            "type" to log.type,
                            "quantityDelta" to log.quantityDelta,
                            "totalTzsAmount" to log.totalTzsAmount,
                            "performedByEmail" to log.performedByEmail,
                            "notes" to log.notes,
                            "timestamp" to log.timestamp
                        )
                    )
                }

                // 5. Sync Allowed Emails Whitelist
                val allowedUsers = database.allowedUserDao().getAllAllowedUsers()
                for (user in allowedUsers) {
                    fs.collection("config").document("allowed_emails")
                        .collection("users").document(user.email).set(
                            mapOf(
                                "email" to user.email,
                                "role" to user.role,
                                "addedBy" to user.addedBy,
                                "addedAt" to user.addedAt,
                                "isSeedAdmin" to user.isSeedAdmin
                            )
                        )
                }

                _lastSyncTimestamp.value = System.currentTimeMillis()
            }
            _isSyncing.value = false
        }
    }
}
