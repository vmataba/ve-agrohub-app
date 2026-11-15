package com.tabaapps.ve_agrohub_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tabaapps.ve_agrohub_app.data.local.entity.StockLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Stock Audit Log operations using strictly lowercase SQL syntax.
 *
 * Java Educational Note:
 * Equivalent to a Java repository handling audit trail records.
 * Uses reactive `Flow<List<StockLogEntity>>` streams to notify UI components of new sales, refills, or payments.
 */
@Dao
interface StockLogDao {

    @Query("select * from stock_logs order by timestamp desc")
    fun getAllStockLogsFlow(): Flow<List<StockLogEntity>>

    @Query("select * from stock_logs order by timestamp desc")
    suspend fun getAllStockLogs(): List<StockLogEntity>

    @Query("select * from stock_logs where store_id = :storeId order by timestamp desc")
    fun getStockLogsByStoreFlow(storeId: String): Flow<List<StockLogEntity>>

    @Query("select * from stock_logs where store_id = :storeId order by timestamp desc")
    suspend fun getStockLogsByStore(storeId: String): List<StockLogEntity>

    @Query("select * from stock_logs where id = :id limit 1")
    suspend fun getStockLogById(id: String): StockLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockLog(stockLog: StockLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockLogs(stockLogs: List<StockLogEntity>)

    @Query("delete from stock_logs where id = :id")
    suspend fun deleteStockLogById(id: String)

    @Query("delete from stock_logs")
    suspend fun deleteAllStockLogs()
}
