package com.tabaapps.ve_agrohub_app.data.repository

import com.tabaapps.ve_agrohub_app.data.model.Conversation
import com.tabaapps.ve_agrohub_app.data.model.ConversationWithMessages
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    fun getConversationWithMessages(conversationId: String): Flow<ConversationWithMessages?>
    fun getApiKey(): Flow<String>
    
    suspend fun saveApiKey(apiKey: String)
    suspend fun createNewConversation(title: String = "New Chat"): Conversation
    suspend fun deleteConversation(conversationId: String)
    suspend fun updateConversationTitle(conversationId: String, title: String)
    suspend fun sendMessageAndStreamResponse(
        conversationId: String,
        prompt: String
    ): Flow<String>
}
