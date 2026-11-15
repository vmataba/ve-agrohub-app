package com.tabaapps.ve_agrohub_app.data.local

import androidx.room.TypeConverter
import com.tabaapps.ve_agrohub_app.data.model.MessageRole

class Converters {
    @TypeConverter
    fun fromMessageRole(role: MessageRole): String = role.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = try {
        MessageRole.valueOf(value)
    } catch (e: Exception) {
        MessageRole.ASSISTANT
    }
}
