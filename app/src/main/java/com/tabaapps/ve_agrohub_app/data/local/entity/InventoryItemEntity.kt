package com.tabaapps.ve_agrohub_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a stock inventory item located at a specific store.
 *
 * Java Educational Note:
 * `@ForeignKey` in Room configures relational foreign key constraints in SQLite.
 * In Java enterprise frameworks (JPA / Hibernate), this is equivalent to:
 * `@ManyToOne` and `@JoinColumn(name = "store_id", referencedColumnName = "id")`.
 * Indices (`@Index`) optimize SQL queries filtering by store or product type in the database engine.
 */
@Entity(
    tableName = "inventory_items",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_type_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["store_id"]),
        Index(value = ["product_type_id"])
    ]
)
data class InventoryItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "store_id")
    val storeId: String,

    @ColumnInfo(name = "product_type_id")
    val productTypeId: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "min_stock_alert_threshold")
    val minStockAlertThreshold: Int = 10,

    @ColumnInfo(name = "image_capture_count")
    val imageCaptureCount: Int = 0,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
