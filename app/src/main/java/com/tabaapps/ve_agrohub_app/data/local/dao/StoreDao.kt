package com.tabaapps.ve_agrohub_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tabaapps.ve_agrohub_app.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Store operations using strictly lowercase SQL syntax.
 *
 * Java Educational Note:
 * A Room `@Dao` interface is directly equivalent to a Spring Data JPA Repository interface in Java
 * (e.g., `public interface StoreRepository extends JpaRepository<StoreEntity, String>`).
 * `Flow<List<StoreEntity>>` provides reactive stream updates, similar to RxJava `Observable<List<StoreEntity>>`
 * or `LiveData<List<StoreEntity>>` in traditional Java Android apps.
 * `suspend fun` represents asynchronous non-blocking function execution, analogous to returning `CompletableFuture<Void>` in Java.
 */
@Dao
interface StoreDao {

    @Query("select * from stores order by name asc")
    fun getAllStoresFlow(): Flow<List<StoreEntity>>

    @Query("select * from stores order by name asc")
    suspend fun getAllStores(): List<StoreEntity>

    @Query("select * from stores where id = :id limit 1")
    suspend fun getStoreById(id: String): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)

    @Update
    suspend fun updateStore(store: StoreEntity)

    @Query("delete from stores where id = :id")
    suspend fun deleteStoreById(id: String)

    @Query("delete from stores")
    suspend fun deleteAllStores()
}
