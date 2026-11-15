package com.tabaapps.ve_agrohub_app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tabaapps.ve_agrohub_app.data.model.Conversation
import com.tabaapps.ve_agrohub_app.data.model.SuggestionChipItem
import com.tabaapps.ve_agrohub_app.data.repository.ConversationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var messagesJob: Job? = null
    private var streamingJob: Job? = null

    init {
        observeConversations()
        observeApiKey()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            conversationRepository.getAllConversations().collectLatest { convs ->
                _uiState.update { currentState ->
                    currentState.copy(conversations = convs)
                }
                // If no active conversation, auto-select latest or create new if needed
                if (_uiState.value.currentConversation == null && convs.isNotEmpty()) {
                    selectConversation(convs.first().id)
                }
            }
        }
    }

    private fun observeApiKey() {
        viewModelScope.launch {
            conversationRepository.getApiKey().collectLatest { key ->
                _uiState.update { it.copy(apiKey = key) }
            }
        }
    }

    fun onPromptInputChanged(newInput: String) {
        _uiState.update { it.copy(promptInput = newInput) }
    }

    fun selectConversation(conversationId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            conversationRepository.getConversationWithMessages(conversationId).collectLatest { convWithMsgs ->
                _uiState.update { currentState ->
                    currentState.copy(
                        currentConversation = convWithMsgs?.conversation ?: Conversation(
                            id = conversationId,
                            title = "Conversation"
                        ),
                        messages = convWithMsgs?.messages ?: emptyList()
                    )
                }
            }
        }
    }

    fun createNewConversation() {
        messagesJob?.cancel()
        streamingJob?.cancel()
        val newId = UUID.randomUUID().toString()
        _uiState.update {
            it.copy(
                currentConversation = Conversation(id = newId, title = "New Chat"),
                messages = emptyList(),
                promptInput = "",
                isStreaming = false
            )
        }
    }

    fun sendMessage(promptText: String? = null) {
        val actualPrompt = (promptText ?: _uiState.value.promptInput).trim()
        if (actualPrompt.isBlank() || _uiState.value.isStreaming) return

        val activeConvId = _uiState.value.currentConversation?.id ?: UUID.randomUUID().toString()

        _uiState.update {
            it.copy(
                promptInput = "",
                isStreaming = true,
                errorMessage = null,
                currentConversation = it.currentConversation ?: Conversation(id = activeConvId, title = "New Chat")
            )
        }

        streamingJob?.cancel()
        streamingJob = viewModelScope.launch {
            try {
                // Ensure messages listener is listening to this active conversation
                selectConversation(activeConvId)

                conversationRepository.sendMessageAndStreamResponse(activeConvId, actualPrompt)
                    .collect { _ ->
                        // Content updates directly via Room Flow in selectConversation
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.localizedMessage ?: "Failed to generate response"
                    )
                }
            } finally {
                _uiState.update { it.copy(isStreaming = false) }
            }
        }
    }

    fun onSuggestionChipClicked(chip: SuggestionChipItem) {
        sendMessage(chip.prompt)
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(conversationId)
            if (_uiState.value.currentConversation?.id == conversationId) {
                createNewConversation()
            }
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            conversationRepository.saveApiKey(key)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
