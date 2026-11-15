package com.tabaapps.ve_agrohub_app

import androidx.room.Query
import com.tabaapps.ve_agrohub_app.data.local.dao.AllowedUserDao
import com.tabaapps.ve_agrohub_app.data.local.dao.InventoryItemDao
import com.tabaapps.ve_agrohub_app.data.local.dao.ProductTypeDao
import com.tabaapps.ve_agrohub_app.data.local.dao.StockLogDao
import com.tabaapps.ve_agrohub_app.data.local.dao.StoreDao
import com.tabaapps.ve_agrohub_app.data.model.AllowedUser
import com.tabaapps.ve_agrohub_app.data.model.InventoryItem
import com.tabaapps.ve_agrohub_app.data.model.InventoryItemWithDetails
import com.tabaapps.ve_agrohub_app.data.model.ProductType
import com.tabaapps.ve_agrohub_app.data.model.Store
import com.tabaapps.ve_agrohub_app.util.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying ve-stock core logic, TZS formatting, and mandatory lowercase SQL query compliance.
 *
 * Java Educational Note:
 * JUnit 4 (`@Test`, `assertEquals`, `assertTrue`) is the standard Java testing framework used across
 * Kotlin and Java Android projects. Java Reflection (`Method.getAnnotation(Query::class.java)`) is used
 * to inspect compile-time Room DAO metadata.
 */
class VeStockUnitTest {

    @Test
    fun currencyUtils_formatsTzsAmountCorrectly() {
        assertEquals("TZS 50,000", CurrencyUtils.formatTzsAmount(50000L))
        assertEquals("TZS 1,250,000", CurrencyUtils.formatTzsAmount(1250000L))
        assertEquals("TZS 0", CurrencyUtils.formatTzsAmount(0L))
    }

    @Test
    fun inventoryItemWithDetails_calculatesTotalValueAndLowStock() {
        val store = Store("store_1", "Arusha Central", "Arusha")
        val product = ProductType("prod_1", "NPK 50kg", "Fertilizer", priceTzs = 120000L)

        val healthyItem = InventoryItem("inv_1", store.id, product.id, quantity = 50, minStockAlertThreshold = 10)
        val healthyDetails = InventoryItemWithDetails(healthyItem, store, product)

        assertEquals(6000000L, healthyDetails.totalValueTzs)
        assertEquals("TZS 6,000,000", healthyDetails.formattedTotalValue)
        assertFalse(healthyDetails.isLowStock)

        val lowStockItem = InventoryItem("inv_2", store.id, product.id, quantity = 5, minStockAlertThreshold = 10)
        val lowStockDetails = InventoryItemWithDetails(lowStockItem, store, product)

        assertTrue(lowStockDetails.isLowStock)
    }

    @Test
    fun allowedUser_roleChecksFunctionCorrectly() {
        val adminUser = AllowedUser("admin@veagrohub.co.tz", "ADMIN", "SYSTEM")
        val staffUser = AllowedUser("clerk@veagrohub.co.tz", "STAFF", "SYSTEM")

        assertTrue(adminUser.isAdmin)
        assertFalse(staffUser.isAdmin)
    }

    @Test
    fun verifyAllDaoSqlQueriesUseStrictlyLowercaseSqlKeywords() {
        val daoClasses = listOf(
            StoreDao::class.java,
            ProductTypeDao::class.java,
            InventoryItemDao::class.java,
            StockLogDao::class.java,
            AllowedUserDao::class.java
        )

        val uppercaseSqlRegex = Regex("\\b(SELECT|FROM|WHERE|ORDER BY|ASC|DESC|INSERT|INTO|VALUES|UPDATE|SET|DELETE|LIMIT)\\b")

        for (daoClass in daoClasses) {
            for (method in daoClass.declaredMethods) {
                val queryAnnotation = method.getAnnotation(Query::class.java)
                if (queryAnnotation != null) {
                    val sql = queryAnnotation.value
                    assertFalse(
                        "DAO SQL query in ${daoClass.simpleName}.${method.name}() must use strictly lowercase SQL keywords! Query: $sql",
                        uppercaseSqlRegex.containsMatchIn(sql)
                    )
                }
            }
        }
    }
}
