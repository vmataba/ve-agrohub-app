package com.tabaapps.ve_agrohub_app.data.repository

import com.tabaapps.ve_agrohub_app.data.local.VeStockDatabase
import com.tabaapps.ve_agrohub_app.data.local.entity.InventoryItemEntity
import com.tabaapps.ve_agrohub_app.data.local.entity.ProductTypeEntity
import com.tabaapps.ve_agrohub_app.data.local.entity.StockLogEntity
import com.tabaapps.ve_agrohub_app.data.local.entity.StoreEntity
import com.tabaapps.ve_agrohub_app.data.model.AllowedUser
import com.tabaapps.ve_agrohub_app.data.model.InventoryItem
import com.tabaapps.ve_agrohub_app.data.model.InventoryItemWithDetails
import com.tabaapps.ve_agrohub_app.data.model.ProductType
import com.tabaapps.ve_agrohub_app.data.model.StockLog
import com.tabaapps.ve_agrohub_app.data.model.StockLogWithDetails
import com.tabaapps.ve_agrohub_app.data.model.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Main Repository combining local Room DB operations with domain mapping.
 *
 * Java Educational Note:
 * In Java architecture (Spring / Android Clean Architecture), this class implements the Repository Pattern,
 * acting as a single source of truth that abstracts the underlying persistence layer (Room SQLite / Firestore)
 * away from the ViewModel and UI layer.
 */
class VeStockRepository(
    private val database: VeStockDatabase
) {

    private val storeDao = database.storeDao()
    private val productTypeDao = database.productTypeDao()
    private val inventoryItemDao = database.inventoryItemDao()
    private val stockLogDao = database.stockLogDao()
    private val allowedUserDao = database.allowedUserDao()

    // --- Stores ---
    fun getStoresFlow(): Flow<List<Store>> {
        return storeDao.getAllStoresFlow().map { entities ->
            entities.map { Store(it.id, it.name, it.location, it.updatedAt) }
        }
    }

    suspend fun addStore(name: String, location: String): Store {
        val store = StoreEntity(
            id = "store_${UUID.randomUUID()}",
            name = name,
            location = location
        )
        storeDao.insertStore(store)
        return Store(store.id, store.name, store.location, store.updatedAt)
    }

    suspend fun updateStore(store: Store) {
        storeDao.updateStore(StoreEntity(store.id, store.name, store.location, store.updatedAt))
    }

    suspend fun deleteStore(storeId: String) {
        storeDao.deleteStoreById(storeId)
    }

    // --- Product Types ---
    fun getProductTypesFlow(): Flow<List<ProductType>> {
        return productTypeDao.getAllProductTypesFlow().map { entities ->
            entities.map { ProductType(it.id, it.name, it.category, it.brand, it.unitSize, it.priceTzs, it.updatedAt) }
        }
    }

    suspend fun addProductType(
        name: String,
        category: String,
        priceTzs: Long,
        brand: String = "General",
        unitSize: String = "50 kg Bag"
    ): ProductType {
        val entity = ProductTypeEntity(
            id = "prod_${UUID.randomUUID()}",
            name = name,
            category = category,
            brand = brand,
            unitSize = unitSize,
            priceTzs = priceTzs
        )
        productTypeDao.insertProductType(entity)
        return ProductType(entity.id, entity.name, entity.category, entity.brand, entity.unitSize, entity.priceTzs, entity.updatedAt)
    }

    suspend fun updateProductType(productType: ProductType) {
        productTypeDao.updateProductType(
            ProductTypeEntity(
                id = productType.id,
                name = productType.name,
                category = productType.category,
                brand = productType.brand,
                unitSize = productType.unitSize,
                priceTzs = productType.priceTzs,
                updatedAt = productType.updatedAt
            )
        )
    }

    suspend fun deleteProductType(productTypeId: String) {
        productTypeDao.deleteProductTypeById(productTypeId)
    }

    // --- Inventory Items with Details ---
    fun getInventoryItemsWithDetailsFlow(): Flow<List<InventoryItemWithDetails>> {
        return combine(
            inventoryItemDao.getAllInventoryItemsFlow(),
            storeDao.getAllStoresFlow(),
            productTypeDao.getAllProductTypesFlow()
        ) { inventoryEntities, storeEntities, productEntities ->
            val storeMap = storeEntities.associateBy { it.id }
            val productMap = productEntities.associateBy { it.id }

            inventoryEntities.map { itemEntity ->
                val storeEntity = storeMap[itemEntity.storeId]
                val productEntity = productMap[itemEntity.productTypeId]

                InventoryItemWithDetails(
                    item = InventoryItem(
                        id = itemEntity.id,
                        storeId = itemEntity.storeId,
                        productTypeId = itemEntity.productTypeId,
                        quantity = itemEntity.quantity,
                        minStockAlertThreshold = itemEntity.minStockAlertThreshold,
                        imageCaptureCount = itemEntity.imageCaptureCount,
                        updatedAt = itemEntity.updatedAt
                    ),
                    store = storeEntity?.let { Store(it.id, it.name, it.location, it.updatedAt) },
                    productType = productEntity?.let { ProductType(it.id, it.name, it.category, it.brand, it.unitSize, it.priceTzs, it.updatedAt) }
                )
            }
        }
    }

    suspend fun updateInventoryQuantity(itemId: String, newQuantity: Int) {
        inventoryItemDao.updateQuantity(itemId, newQuantity.coerceAtLeast(0))
    }

    suspend fun incrementImageCaptureCount(itemId: String) {
        inventoryItemDao.incrementImageCaptureCount(itemId)
    }

    // --- Quick Stock Taking Actions ---
    /**
     * Records a Sale, Refill, or Pending Payment transaction.
     * Updates inventory quantity and logs an audit record in `stock_logs`.
     */
    suspend fun recordStockTransaction(
        storeId: String,
        productTypeId: String,
        transactionType: String, // "SALE", "REFILL", "PENDING_PAYMENT"
        quantityDelta: Int,
        userEmail: String,
        customerName: String = "",
        notes: String = ""
    ) {
        val existingItem = inventoryItemDao.getInventoryItemByStoreAndProductType(storeId, productTypeId)
        val productType = productTypeDao.getProductTypeById(productTypeId)
        val unitPrice = productType?.priceTzs ?: 0L

        val newQuantity = if (existingItem != null) {
            (existingItem.quantity + quantityDelta).coerceAtLeast(0)
        } else {
            quantityDelta.coerceAtLeast(0)
        }

        val itemId = existingItem?.id ?: "inv_${UUID.randomUUID()}"
        val updatedItem = InventoryItemEntity(
            id = itemId,
            storeId = storeId,
            productTypeId = productTypeId,
            quantity = newQuantity,
            minStockAlertThreshold = existingItem?.minStockAlertThreshold ?: 10,
            imageCaptureCount = existingItem?.imageCaptureCount ?: 0,
            updatedAt = System.currentTimeMillis()
        )
        inventoryItemDao.insertInventoryItem(updatedItem)

        // Calculate total TZS value for transaction
        val totalTzs = Math.abs(quantityDelta) * unitPrice

        // Log transaction audit trail
        val logEntity = StockLogEntity(
            id = "log_${UUID.randomUUID()}",
            storeId = storeId,
            productTypeId = productTypeId,
            type = transactionType,
            quantityDelta = quantityDelta,
            totalTzsAmount = totalTzs,
            performedByEmail = userEmail,
            customerName = customerName,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        stockLogDao.insertStockLog(logEntity)
    }

    // --- Stock Logs ---
    fun getStockLogsWithDetailsFlow(): Flow<List<StockLogWithDetails>> {
        return combine(
            stockLogDao.getAllStockLogsFlow(),
            storeDao.getAllStoresFlow(),
            productTypeDao.getAllProductTypesFlow()
        ) { logEntities, storeEntities, productEntities ->
            val storeMap = storeEntities.associateBy { it.id }
            val productMap = productEntities.associateBy { it.id }

            logEntities.map { logEntity ->
                val storeName = storeMap[logEntity.storeId]?.name ?: "Unknown Store"
                val productName = productMap[logEntity.productTypeId]?.name ?: "Unknown Product"

                StockLogWithDetails(
                    log = StockLog(
                        id = logEntity.id,
                        storeId = logEntity.storeId,
                        productTypeId = logEntity.productTypeId,
                        type = logEntity.type,
                        quantityDelta = logEntity.quantityDelta,
                        totalTzsAmount = logEntity.totalTzsAmount,
                        performedByEmail = logEntity.performedByEmail,
                        customerName = logEntity.customerName,
                        notes = logEntity.notes,
                        timestamp = logEntity.timestamp
                    ),
                    storeName = storeName,
                    productName = productName
                )
            }
        }
    }

    // --- Allowed Users Whitelist ---
    fun getAllowedUsersFlow(): Flow<List<AllowedUser>> {
        return allowedUserDao.getAllAllowedUsersFlow().map { entities ->
            entities.map {
                AllowedUser(it.email, it.role, it.addedBy, it.addedAt, it.isSeedAdmin)
            }
        }
    }
}
