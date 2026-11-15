package com.tabaapps.ve_agrohub_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tabaapps.ve_agrohub_app.data.local.entity.AllowedUserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for AllowedUser whitelist RBAC operations using strictly lowercase SQL syntax.
 *
 * Java Educational Note:
 * Equivalent to a Java security repository storing user authorization records.
 */
@Dao
interface AllowedUserDao {

    @Query("select * from allowed_users order by email asc")
    fun getAllAllowedUsersFlow(): Flow<List<AllowedUserEntity>>

    @Query("select * from allowed_users order by email asc")
    suspend fun getAllAllowedUsers(): List<AllowedUserEntity>

    @Query("select * from allowed_users where email = :email limit 1")
    suspend fun getAllowedUserByEmail(email: String): AllowedUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowedUser(user: AllowedUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowedUsers(users: List<AllowedUserEntity>)

    @Update
    suspend fun updateAllowedUser(user: AllowedUserEntity)

    @Query("delete from allowed_users where email = :email")
    suspend fun deleteAllowedUserByEmail(email: String)

    @Query("delete from allowed_users")
    suspend fun deleteAllAllowedUsers()
}
