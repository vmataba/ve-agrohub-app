package com.tabaapps.ve_agrohub_app.ui.chat

import com.tabaapps.ve_agrohub_app.data.model.ChatMessage
import com.tabaapps.ve_agrohub_app.data.model.Conversation
import com.tabaapps.ve_agrohub_app.data.model.SuggestionChipItem

data class ChatUiState(
    val currentConversation: Conversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val suggestionChips: List<SuggestionChipItem> = defaultSuggestionChips,
    val promptInput: String = "",
    val isStreaming: Boolean = false,
    val apiKey: String = "",
    val errorMessage: String? = null
)

val defaultSuggestionChips = listOf(
    SuggestionChipItem(
        id = "1",
        title = "Quantum Computing",
        prompt = "Explain quantum computing simply with an analogy",
        iconName = "AutoAwesome"
    ),
    SuggestionChipItem(
        id = "2",
        title = "Spinach & Eggs",
        prompt = "Give me a quick 10-minute recipe with eggs and spinach",
        iconName = "Restaurant"
    ),
    SuggestionChipItem(
        id = "3",
        title = "Fix Flat Tire",
        prompt = "How to safely change a flat tire step-by-step",
        iconName = "Build"
    ),
    SuggestionChipItem(
        id = "4",
        title = "Python Script",
        prompt = "Write a Python script to organize files by extension",
        iconName = "Code"
    )
)
