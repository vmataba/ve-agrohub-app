package com.tabaapps.ve_agrohub_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity tracking audit trail records for stock transactions (SALE, REFILL, PENDING_PAYMENT).
 *
 * Java Educational Note:
 * Equivalent to a Java persistence entity mapped to `stock_logs` audit log table.
 * Corresponds to a Java POJO/Record with timestamp and performee tracking.
 */
@Entity(tableName = "stock_logs")
data class StockLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "store_id")
    val storeId: String,

    @ColumnInfo(name = "product_type_id")
    val productTypeId: String,

    @ColumnInfo(name = "type")
    val type: String, // "SALE", "REFILL", "PENDING_PAYMENT"

    @ColumnInfo(name = "quantity_delta")
    val quantityDelta: Int,

    @ColumnInfo(name = "total_tzs_amount")
    val totalTzsAmount: Long,

    @ColumnInfo(name = "performed_by_email")
    val performedByEmail: String,

    @ColumnInfo(name = "customer_name")
    val customerName: String = "",

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
