package com.tabaapps.ve_agrohub_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tabaapps.ve_agrohub_app.data.local.dao.AllowedUserDao
import com.tabaapps.ve_agrohub_app.data.local.dao.InventoryItemDao
import com.tabaapps.ve_agrohub_app.data.local.dao.ProductTypeDao
import com.tabaapps.ve_agrohub_app.data.local.dao.StockLogDao
import com.tabaapps.ve_agrohub_app.data.local.dao.StoreDao
import com.tabaapps.ve_agrohub_app.data.local.entity.AllowedUserEntity
import com.tabaapps.ve_agrohub_app.data.local.entity.InventoryItemEntity
import com.tabaapps.ve_agrohub_app.data.local.entity.ProductTypeEntity
import com.tabaapps.ve_agrohub_app.data.local.entity.StockLogEntity
import com.tabaapps.ve_agrohub_app.data.local.entity.StoreEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database for ve-stock application ("ve_stock_db").
 *
 * Java Educational Note:
 * `@Database` generates an abstract class implementation at compile time.
 * In Java Android, this is the Singleton Database pattern (`synchronized` double-checked locking).
 * In Kotlin, `@Volatile` combined with `synchronized(this)` ensures thread-safe instantiation
 * across multiple threads/coroutines.
 */
@Database(
    entities = [
        StoreEntity::class,
        ProductTypeEntity::class,
        InventoryItemEntity::class,
        StockLogEntity::class,
        AllowedUserEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class VeStockDatabase : RoomDatabase() {

    abstract fun storeDao(): StoreDao
    abstract fun productTypeDao(): ProductTypeDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun stockLogDao(): StockLogDao
    abstract fun allowedUserDao(): AllowedUserDao

    companion object {
        @Volatile
        private var INSTANCE: VeStockDatabase? = null

        fun getDatabase(context: Context): VeStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VeStockDatabase::class.java,
                    "ve_stock_db",
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: VeStockDatabase) {
                // Initial Single Store: VE AgroHub Kibaigwa
                val stores = listOf(
                    StoreEntity("store_1", "VE AgroHub Kibaigwa", "Kibaigwa, Dodoma, Tanzania")
                )
                db.storeDao().insertStores(stores)

                // Initial Koudijs Concentrates & Irvine's Product Catalog
                val products = listOf(
                    // --- KOUDIJS CONCENTRATES ---
                    ProductTypeEntity("prod_k1", "Koudijs Broiler Starter Concentrate 10%", "Starter", "Koudijs", "50 kg Bag", 165000L),
                    ProductTypeEntity("prod_k2", "Koudijs Piglet / Chick Starter Concentrate 15%", "Starter", "Koudijs", "50 kg Bag", 145000L),
                    ProductTypeEntity("prod_k3", "Koudijs Broiler Finisher Concentrate 10%", "Grower", "Koudijs", "50 kg Bag", 160000L),
                    ProductTypeEntity("prod_k4", "Koudijs Pig Grower Concentrate 5%", "Grower", "Koudijs", "50 kg Bag", 135000L),
                    ProductTypeEntity("prod_k5", "Koudijs Layer Concentrate 5%", "Layer Food", "Koudijs", "50 kg Bag", 175000L),
                    ProductTypeEntity("prod_k6", "Koudijs Layer Concentrate 25%", "Layer Food", "Koudijs", "50 kg Bag", 155000L),
                    ProductTypeEntity("prod_k7", "Koudijs Dairy Cattle Concentrate 10%", "Concentrate", "Koudijs", "50 kg Bag", 130000L),

                    // --- IRVINE'S PRODUCTS ---
                    ProductTypeEntity("prod_i1", "Irvine's Broiler Starter Crumble", "Starter", "Irvine's", "50 kg Bag", 90000L),
                    ProductTypeEntity("prod_i2", "Irvine's Chick / Pullet Starter Mash", "Starter", "Irvine's", "50 kg Bag", 84000L),
                    ProductTypeEntity("prod_i3", "Cobb 500 Broiler Day-Old Chicks", "Starter", "Irvine's", "Box (100 Chicks)", 230000L),
                    ProductTypeEntity("prod_i4", "Irvine's Broiler Finisher / Grower Pellets", "Grower", "Irvine's", "50 kg Bag", 86000L),
                    ProductTypeEntity("prod_i5", "Irvine's Pullet Grower Mash", "Grower", "Irvine's", "50 kg Bag", 78000L),
                    ProductTypeEntity("prod_i6", "Irvine's Layer Mash Phase 1 (Peak Laying)", "Layer Food", "Irvine's", "50 kg Bag", 80000L),
                    ProductTypeEntity("prod_i7", "Bovans Brown Layer Day-Old Chicks", "Layer Food", "Irvine's", "Box (100 Chicks)", 340000L)
                )
                db.productTypeDao().insertProductTypes(products)

                // Initial Inventory Items assigned to VE AgroHub Kibaigwa
                val inventory = listOf(
                    InventoryItemEntity("inv_1", "store_1", "prod_k1", quantity = 45, minStockAlertThreshold = 10),
                    InventoryItemEntity("inv_2", "store_1", "prod_k2", quantity = 20, minStockAlertThreshold = 8),
                    InventoryItemEntity("inv_3", "store_1", "prod_k3", quantity = 35, minStockAlertThreshold = 10),
                    InventoryItemEntity("inv_4", "store_1", "prod_k5", quantity = 50, minStockAlertThreshold = 15),
                    InventoryItemEntity("inv_5", "store_1", "prod_k6", quantity = 8, minStockAlertThreshold = 12), // Low stock
                    InventoryItemEntity("inv_6", "store_1", "prod_i1", quantity = 60, minStockAlertThreshold = 15),
                    InventoryItemEntity("inv_7", "store_1", "prod_i3", quantity = 15, minStockAlertThreshold = 5),
                    InventoryItemEntity("inv_8", "store_1", "prod_i4", quantity = 40, minStockAlertThreshold = 10),
                    InventoryItemEntity("inv_9", "store_1", "prod_i6", quantity = 55, minStockAlertThreshold = 15),
                    InventoryItemEntity("inv_10", "store_1", "prod_i7", quantity = 6, minStockAlertThreshold = 8) // Low stock
                )
                db.inventoryItemDao().insertInventoryItems(inventory)

                // Initial Seed Admins
                val seedAdmins = listOf(
                    AllowedUserEntity("admin@veagrohub.co.tz", "ADMIN", "SYSTEM", isSeedAdmin = true),
                    AllowedUserEntity("manager@veagrohub.co.tz", "ADMIN", "SYSTEM", isSeedAdmin = true),
                    AllowedUserEntity("staff@veagrohub.co.tz", "STAFF", "SYSTEM", isSeedAdmin = false)
                )
                db.allowedUserDao().insertAllowedUsers(seedAdmins)

                // Initial Stock Log
                db.stockLogDao().insertStockLog(
                    StockLogEntity(
                        id = "log_1",
                        storeId = "store_1",
                        productTypeId = "prod_k5",
                        type = "REFILL",
                        quantityDelta = 50,
                        totalTzsAmount = 8750000L,
                        performedByEmail = "admin@veagrohub.co.tz",
                        notes = "Initial Koudijs Layer Concentrate stock arrival at Kibaigwa"
                    )
                )
            }
        }
    }
}
