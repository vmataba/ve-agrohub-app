package com.tabaapps.ve_agrohub_app.data.model

import com.tabaapps.ve_agrohub_app.util.CurrencyUtils

/**
 * Domain Models for ve-stock app.
 *
 * Java Educational Note:
 * In Java 14+, these domain models correspond to Immutable Records:
 * e.g., `public record Store(String id, String name, String location, long updatedAt) {}`
 * In older Java, these would be POJOs with private fields, constructor, getters, and overridden
 * `equals()`, `hashCode()`, and `toString()`.
 */

data class Store(
    val id: String,
    val name: String,
    val location: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ProductType(
    val id: String,
    val name: String,
    val category: String,
    val brand: String = "General",
    val unitSize: String = "50 kg Bag",
    val priceTzs: Long,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Formatted price string in Tanzanian Shillings.
     */
    val formattedPrice: String
        get() = CurrencyUtils.formatTzsAmount(priceTzs)

    /**
     * Full display name including Brand and Unit Packaging.
     */
    val fullDisplayName: String
        get() = if (brand.isNotBlank() && !brand.equals("General", ignoreCase = true)) {
            "[$brand] $name ($unitSize)"
        } else {
            "$name ($unitSize)"
        }
}

data class InventoryItem(
    val id: String,
    val storeId: String,
    val productTypeId: String,
    val quantity: Int,
    val minStockAlertThreshold: Int = 10,
    val imageCaptureCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Composite UI model combining InventoryItem with its associated Store and ProductType.
 *
 * Java Educational Note:
 * Analogous to a Data Transfer Object (DTO) in Java enterprise systems created from a SQL JOIN query
 * or mapping multiple entities into a presentation object.
 */
data class InventoryItemWithDetails(
    val item: InventoryItem,
    val store: Store?,
    val productType: ProductType?
) {
    val storeName: String
        get() = store?.name ?: "Unknown Store"

    val productName: String
        get() = productType?.name ?: "Unknown Product"

    val category: String
        get() = productType?.category ?: "General"

    val brand: String
        get() = productType?.brand ?: "General"

    val unitSize: String
        get() = productType?.unitSize ?: "50 kg Bag"

    val unitPriceTzs: Long
        get() = productType?.priceTzs ?: 0L

    val formattedUnitPrice: String
        get() = CurrencyUtils.formatTzsAmount(unitPriceTzs)

    val totalValueTzs: Long
        get() = item.quantity * unitPriceTzs

    val formattedTotalValue: String
        get() = CurrencyUtils.formatTzsAmount(totalValueTzs)

    val isLowStock: Boolean
        get() = item.quantity <= item.minStockAlertThreshold
}

data class StockLog(
    val id: String,
    val storeId: String,
    val productTypeId: String,
    val type: String, // "SALE", "REFILL", "PENDING_PAYMENT"
    val quantityDelta: Int,
    val totalTzsAmount: Long,
    val performedByEmail: String,
    val customerName: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class StockLogWithDetails(
    val log: StockLog,
    val storeName: String,
    val productName: String
) {
    val formattedAmount: String
        get() = CurrencyUtils.formatTzsAmount(log.totalTzsAmount)
}

data class AllowedUser(
    val email: String,
    val role: String, // "ADMIN" or "STAFF"
    val addedBy: String,
    val addedAt: Long = System.currentTimeMillis(),
    val isSeedAdmin: Boolean = false
) {
    val isAdmin: Boolean
        get() = role.equals("ADMIN", ignoreCase = true)
}

/**
 * Security Role Enum for 3-tier Whitelist RBAC system.
 *
 * Java Educational Note:
 * Directly maps to Java `public enum UserRole { ADMIN, STAFF, NONE }`.
 */
enum class UserRole {
    ADMIN,
    STAFF,
    NONE
}

enum class AuthStatus {
    AUTHENTICATED,
    ACCESS_DENIED,
    UNAUTHENTICATED
}
