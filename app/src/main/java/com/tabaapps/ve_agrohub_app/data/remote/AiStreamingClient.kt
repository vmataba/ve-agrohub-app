package com.tabaapps.ve_agrohub_app.data.remote

import com.tabaapps.ve_agrohub_app.data.model.ChatMessage
import com.tabaapps.ve_agrohub_app.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class AiStreamingClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    fun streamResponse(
        prompt: String,
        history: List<ChatMessage> = emptyList(),
        apiKey: String = "",
        model: String = "gpt-3.5-turbo"
    ): Flow<String> = flow {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            // Smooth mock typewriter streaming
            generateMockResponse(prompt).collect { chunk ->
                emit(chunk)
            }
        } else {
            // Real API SSE streaming call
            try {
                streamRealApiResponse(prompt, history, trimmedKey, model).collect { chunk ->
                    emit(chunk)
                }
            } catch (e: Exception) {
                // If real API fails, emit error explanation or fallback smoothly
                val errorMsg = "\n\n⚠️ **API Request Error:** ${e.localizedMessage ?: "Failed to connect to AI server"}\n\nFalling back to simulated assistant response:\n\n"
                emit(errorMsg)
                generateMockResponse(prompt).collect { chunk ->
                    emit(chunk)
                }
            }
        }
    }

    private fun streamRealApiResponse(
        prompt: String,
        history: List<ChatMessage>,
        apiKey: String,
        model: String
    ): Flow<String> = flow {
        val url = "https://api.openai.com/v1/chat/completions"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val messagesArray = JSONArray()
        // Add system message
        messagesArray.put(
            JSONObject().apply {
                put("role", "system")
                put("content", "You are a helpful, clear, and insightful AI assistant.")
            }
        )
        // Add conversation history
        for (msg in history) {
            val roleStr = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            }
            messagesArray.put(
                JSONObject().apply {
                    put("role", roleStr)
                    put("content", msg.content)
                }
            )
        }
        // Add current prompt if not already in history
        if (history.isEmpty() || history.last().content != prompt) {
            messagesArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }
            )
        }

        val requestBodyJson = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("stream", true)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBodyJson.toString().toRequestBody(mediaType))
            .build()

        val response = withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            val errorResponseBody = withContext(Dispatchers.IO) { response.body?.string() ?: "" }
            throw Exception("HTTP ${response.code}: $errorResponseBody")
        }

        val inputStream = response.body?.byteStream()
            ?: throw Exception("Empty response body")

        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            while (true) {
                val line = withContext(Dispatchers.IO) { reader.readLine() } ?: break
                if (line.startsWith("data: ")) {
                    val dataStr = line.substring(6).trim()
                    if (dataStr == "[DONE]") {
                        break
                    }
                    try {
                        val json = JSONObject(dataStr)
                        val choices = json.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val delta = choices.getJSONObject(0).optJSONObject("delta")
                            val content = delta?.optString("content")
                            if (!content.isNullOrEmpty()) {
                                emit(content)
                            }
                        }
                    } catch (e: Exception) {
                        // ignore non-json lines
                    }
                }
            }
        } finally {
            withContext(Dispatchers.IO) { reader.close() }
        }
    }

    private fun generateMockResponse(prompt: String): Flow<String> = flow {
        val fullResponse = buildMockTextForPrompt(prompt)
        // Stream token/word by token/word for typewriter effect
        val tokens = fullResponse.split(Regex("(?<=\\s)|(?=\\n)"))
        for (token in tokens) {
            emit(token)
            delay(35L)
        }
    }

    private fun buildMockTextForPrompt(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("quantum") -> """
                ### 🌌 Quantum Computing Explained Simply
                
                Imagine normal computers use tiny light switches that can only be either **0 (Off)** or **1 (On)**. Every bit of information is stored this way.
                
                Quantum computers use **qubits**. Thanks to two amazing laws of quantum physics:
                
                1. **Superposition:** A qubit can be 0, 1, or *both at the same time*, like a spinning coin before it lands.
                2. **Entanglement:** Qubits can be linked together instantly, no matter how far apart they are.
                
                #### 🚀 Why does this matter?
                While a standard computer checks one solution at a time, a quantum computer can analyze **millions of possibilities simultaneously**.
                
                > **Use Cases:** Drug discovery, climate modeling, breaking complex cryptography, and optimizing global logistics.
            """.trimIndent()

            lower.contains("recipe") || lower.contains("egg") || lower.contains("spinach") -> """
                ### 🍳 Quick Spinach & Garlic Egg Scramble
                
                Here is a delicious, healthy meal ready in under 10 minutes!
                
                #### 🛒 Ingredients:
                - 3 Fresh Eggs
                - 1 cup Fresh Baby Spinach
                - 1 clove Garlic (minced)
                - 1 tbsp Olive Oil or Butter
                - Salt, Black Pepper & Red Pepper Flakes to taste
                
                #### 🔪 Instructions:
                1. **Sauté Garlic:** Heat olive oil in a non-stick skillet over medium heat. Add minced garlic and cook for 30 seconds until fragrant.
                2. **Wilt Spinach:** Toss in the fresh spinach leaves and cook for 1 minute until wilted.
                3. **Scramble:** Whisk eggs with salt and pepper. Pour into skillet with spinach. Gently stir with a spatula until soft curds form.
                4. **Serve:** Garnish with red pepper flakes and enjoy with toasted sourdough! 🍞
            """.trimIndent()

            lower.contains("tire") || lower.contains("flat") -> """
                ### 🚗 How to Change a Flat Tire Safely
                
                Follow these step-by-step safety instructions:
                
                1. **Find a Safe Location:** Pull over to a flat, straight area off the road. Turn on Hazard Lights and engage the Parking Brake.
                2. **Loosen Lug Nuts:** Remove hubcap if present. Use the lug wrench to loosen lug nuts slightly (about 1/4 turn counterclockwise) *before* lifting the car.
                3. **Jack Up Vehicle:** Place the jack under the vehicle frame metal notch. Raise the car until the flat tire is ~6 inches off the ground.
                4. **Remove Tire & Mount Spare:** Unscrew lug nuts completely, pull off the flat tire, and align spare tire onto lug bolts.
                5. **Tighten Lug Nuts:** Screw nuts back on by hand. Lower vehicle slightly, then fully tighten lug nuts in a **star pattern**.
            """.trimIndent()

            lower.contains("code") || lower.contains("python") || lower.contains("script") -> """
                ### 🐍 Python File Organizer Script
                
                Here is a concise Python script to automatically organize your Downloads folder by file extension:
                
                ```python
                import os
                import shutil
                from pathlib import Path

                def organize_folder(target_dir):
                    extensions = {
                        "Images": [".jpg", ".jpeg", ".png", ".gif"],
                        "Documents": [".pdf", ".docx", ".txt", ".xlsx"],
                        "Archives": [".zip", ".tar", ".gz"],
                        "Media": [".mp3", ".mp4", ".mkv"]
                    }
                    
                    path = Path(target_dir)
                    for item in path.iterdir():
                        if item.is_file():
                            ext = item.suffix.lower()
                            for category, exts in extensions.items():
                                if ext in exts:
                                    dest = path / category
                                    dest.mkdir(exist_ok=True)
                                    shutil.move(str(item), str(dest / item.name))
                                    print(f"Moved {item.name} -> {category}")

                if __name__ == "__main__":
                    organize_folder("./Downloads")
                ```
                
                > **Tip:** You can run this script periodically using `cron` or Windows Task Scheduler!
            """.trimIndent()

            lower.contains("summary") || lower.contains("summarize") -> """
                ### 📝 Summary Overview
                
                Here is a structured summary of key points:
                
                - **Core Theme:** Efficient execution, clear design structure, and fast responsiveness.
                - **Key Takeaways:**
                  - Focus on single-responsibility principles.
                  - Use reactive data flows with Kotlin Coroutines and StateFlow.
                  - Provide clear feedback during background processing.
                - **Conclusion:** By structuring components cleanly, systems remain scalable, testable, and user-friendly.
            """.trimIndent()

            else -> """
                ### 🤖 AI Assistant Response
                
                Thank you for asking! Here is what you should know about **"$prompt"**:
                
                - **Overview:** That's a great question. The topic covers fundamental principles that help streamline understanding and problem solving.
                - **Key Consideration:** Whether you're researching technical concepts or looking for practical everyday solutions, breaking down the problem into smaller actionable steps always yields the best results.
                
                #### 💡 Recommendations:
                1. Explore related examples and edge cases.
                2. Test small iterations to verify assumptions.
                3. Feel free to ask a follow-up question if you'd like deeper detail on any specific aspect!
            """.trimIndent()
        }
    }
}
