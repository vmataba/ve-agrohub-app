package com.tabaapps.ve_agrohub_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a dynamic whitelist entry for Role-Based Access Control (RBAC).
 *
 * Java Educational Note:
 * Equivalent to a Java security model table `allowed_users` mapping user email addresses to security roles.
 * In Java Spring Security, this represents user authorization records (`GrantedAuthority` / Role: ADMIN or STAFF).
 */
@Entity(tableName = "allowed_users")
data class AllowedUserEntity(
    @PrimaryKey
    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "role")
    val role: String, // "ADMIN" or "STAFF"

    @ColumnInfo(name = "added_by")
    val addedBy: String,

    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_seed_admin")
    val isSeedAdmin: Boolean = false
)
