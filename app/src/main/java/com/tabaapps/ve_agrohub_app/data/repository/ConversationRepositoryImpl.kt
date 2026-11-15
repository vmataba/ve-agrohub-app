package com.tabaapps.ve_agrohub_app.data.repository

import com.tabaapps.ve_agrohub_app.data.local.ChatMessageDao
import com.tabaapps.ve_agrohub_app.data.local.ConversationDao
import com.tabaapps.ve_agrohub_app.data.local.UserPreferencesRepository
import com.tabaapps.ve_agrohub_app.data.model.ChatMessage
import com.tabaapps.ve_agrohub_app.data.model.Conversation
import com.tabaapps.ve_agrohub_app.data.model.ConversationWithMessages
import com.tabaapps.ve_agrohub_app.data.model.MessageRole
import com.tabaapps.ve_agrohub_app.data.remote.AiStreamingClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.UUID

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val chatMessageDao: ChatMessageDao,
    private val aiStreamingClient: AiStreamingClient,
    private val userPreferencesRepository: UserPreferencesRepository
) : ConversationRepository {

    override fun getAllConversations(): Flow<List<Conversation>> =
        conversationDao.getAllConversations()

    override fun getConversationWithMessages(conversationId: String): Flow<ConversationWithMessages?> =
        conversationDao.getConversationWithMessages(conversationId)

    override fun getApiKey(): Flow<String> =
        userPreferencesRepository.apiKeyFlow

    override suspend fun saveApiKey(apiKey: String) {
        userPreferencesRepository.saveApiKey(apiKey)
    }

    override suspend fun createNewConversation(title: String): Conversation {
        val newConversation = Conversation(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        conversationDao.insertConversation(newConversation)
        return newConversation
    }

    override suspend fun deleteConversation(conversationId: String) {
        chatMessageDao.deleteMessagesForConversation(conversationId)
        conversationDao.deleteConversation(conversationId)
    }

    override suspend fun updateConversationTitle(conversationId: String, title: String) {
        conversationDao.updateConversationTitle(conversationId, title)
    }

    override suspend fun sendMessageAndStreamResponse(
        conversationId: String,
        prompt: String
    ): Flow<String> = flow {
        // Ensure conversation exists or update title
        var conversation = conversationDao.getConversationById(conversationId)
        if (conversation == null) {
            val title = generateConversationTitle(prompt)
            conversation = Conversation(
                id = conversationId,
                title = title,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            conversationDao.insertConversation(conversation)
        } else if (conversation.title == "New Chat" || conversation.title.isBlank()) {
            val title = generateConversationTitle(prompt)
            conversationDao.updateConversationTitle(conversationId, title)
        } else {
            conversationDao.updateConversationTimestamp(conversationId)
        }

        // Save User Message
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = MessageRole.USER,
            content = prompt.trim(),
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(userMsg)

        // Save Assistant Streaming Placeholder Message
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            conversationId = conversationId,
            role = MessageRole.ASSISTANT,
            content = "",
            timestamp = System.currentTimeMillis() + 1,
            isStreaming = true,
            isError = false
        )
        chatMessageDao.insertMessage(assistantMsg)

        // Fetch History
        val history = chatMessageDao.getMessagesForConversationOnce(conversationId)
            .filter { it.id != assistantMsgId && !it.isStreaming }

        val apiKey = userPreferencesRepository.apiKeyFlow.first()
        val model = userPreferencesRepository.selectedModelFlow.first()

        var accumulatedContent = ""

        try {
            aiStreamingClient.streamResponse(prompt, history, apiKey, model).collect { chunk ->
                accumulatedContent += chunk
                chatMessageDao.updateMessageContent(
                    id = assistantMsgId,
                    content = accumulatedContent,
                    isStreaming = true,
                    isError = false
                )
                emit(accumulatedContent)
            }
            chatMessageDao.updateMessageContent(
                id = assistantMsgId,
                content = accumulatedContent,
                isStreaming = false,
                isError = false
            )
        } catch (e: Exception) {
            val errorContent = if (accumulatedContent.isNotBlank()) {
                "$accumulatedContent\n\n[Error: ${e.localizedMessage ?: "Stream interrupted"}]"
            } else {
                "Sorry, an error occurred while streaming response: ${e.localizedMessage}"
            }
            chatMessageDao.updateMessageContent(
                id = assistantMsgId,
                content = errorContent,
                isStreaming = false,
                isError = true
            )
            emit(errorContent)
        }
    }

    private fun generateConversationTitle(prompt: String): String {
        val clean = prompt.trim().replace("\n", " ")
        return if (clean.length > 30) {
            clean.take(30) + "..."
        } else if (clean.isNotBlank()) {
            clean
        } else {
            "New Conversation"
        }
    }
}
