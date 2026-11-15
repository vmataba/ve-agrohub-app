package com.tabaapps.ve_agrohub_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tabaapps.ve_agrohub_app.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Inventory Item operations using strictly lowercase SQL syntax.
 *
 * Java Educational Note:
 * Analogous to Java Spring Data JPA Repository interface.
 * Room annotations generate implementation classes at compile time (via KSP / Annotation Processors),
 * equivalent to Java's `Proxy` or bytecode generation (e.g., CGLIB / ByteBuddy).
 */
@Dao
interface InventoryItemDao {

    @Query("select * from inventory_items order by id asc")
    fun getAllInventoryItemsFlow(): Flow<List<InventoryItemEntity>>

    @Query("select * from inventory_items order by id asc")
    suspend fun getAllInventoryItems(): List<InventoryItemEntity>

    @Query("select * from inventory_items where store_id = :storeId order by id asc")
    fun getInventoryItemsByStoreFlow(storeId: String): Flow<List<InventoryItemEntity>>

    @Query("select * from inventory_items where store_id = :storeId order by id asc")
    suspend fun getInventoryItemsByStore(storeId: String): List<InventoryItemEntity>

    @Query("select * from inventory_items where id = :id limit 1")
    suspend fun getInventoryItemById(id: String): InventoryItemEntity?

    @Query("select * from inventory_items where store_id = :storeId and product_type_id = :productTypeId limit 1")
    suspend fun getInventoryItemByStoreAndProductType(storeId: String, productTypeId: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    @Query("update inventory_items set quantity = :newQuantity, updated_at = :updatedAt where id = :id")
    suspend fun updateQuantity(id: String, newQuantity: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("update inventory_items set image_capture_count = image_capture_count + 1 where id = :id")
    suspend fun incrementImageCaptureCount(id: String)

    @Query("delete from inventory_items where id = :id")
    suspend fun deleteInventoryItemById(id: String)

    @Query("delete from inventory_items")
    suspend fun deleteAllInventoryItems()
}
