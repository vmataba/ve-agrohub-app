package com.tabaapps.ve_agrohub_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a product catalog entry with Brand, Packaging unit, and TZS pricing.
 *
 * Java Educational Note:
 * Equivalent to a Java persistence entity mapped to table `product_types`.
 * `val priceTzs: Long` uses 64-bit primitive `long` in Java bytecodes to accurately store monetary units
 * without floating point rounding errors (similar to `java.math.BigDecimal` or integer cents/shillings).
 */
@Entity(tableName = "product_types")
data class ProductTypeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "brand")
    val brand: String = "General",

    @ColumnInfo(name = "unit_size")
    val unitSize: String = "50 kg Bag",

    @ColumnInfo(name = "price_tzs")
    val priceTzs: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
