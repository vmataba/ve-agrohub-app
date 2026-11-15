package com.tabaapps.ve_agrohub_app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a physical warehouse or store location in the ve-stock system.
 *
 * Java Educational Note:
 * A Kotlin `data class` is directly analogous to a Java JavaBean / POJO (Plain Old Java Object)
 * or Java 14+ Record (`public record StoreEntity(String id, String name, String location, long updatedAt)`).
 * The `@Entity` annotation is Room's counterpart to JPA/Hibernate's `@Entity` / `@Table(name = "stores")`.
 * `@PrimaryKey` corresponds to JPA's `@Id`.
 * `@ColumnInfo` corresponds to `@Column(name = "...")`.
 */
@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
