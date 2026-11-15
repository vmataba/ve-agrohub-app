package com.tabaapps.ve_agrohub_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tabaapps.ve_agrohub_app.data.local.entity.ProductTypeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Product Type operations using strictly lowercase SQL syntax.
 *
 * Java Educational Note:
 * Analogous to Java Spring Data JPA Repository interface.
 * `Flow<List<ProductTypeEntity>>` emits reactive updates when the underlying database table changes,
 * replacing Java's traditional Observable / Observer design pattern.
 */
@Dao
interface ProductTypeDao {

    @Query("select * from product_types order by name asc")
    fun getAllProductTypesFlow(): Flow<List<ProductTypeEntity>>

    @Query("select * from product_types order by name asc")
    suspend fun getAllProductTypes(): List<ProductTypeEntity>

    @Query("select * from product_types where id = :id limit 1")
    suspend fun getProductTypeById(id: String): ProductTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductType(productType: ProductTypeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductTypes(productTypes: List<ProductTypeEntity>)

    @Update
    suspend fun updateProductType(productType: ProductTypeEntity)

    @Query("delete from product_types where id = :id")
    suspend fun deleteProductTypeById(id: String)

    @Query("delete from product_types")
    suspend fun deleteAllProductTypes()
}
