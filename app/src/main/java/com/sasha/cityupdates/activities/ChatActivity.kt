package com.sasha.cityupdates.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sasha.cityupdates.R
import com.sasha.cityupdates.adapters.ChatAdapter
import com.sasha.cityupdates.models.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvChat: RecyclerView

    // 🔑 Replace with your actual Gemini API key
    private val apiKey = "AIzaSyBKnhS-gsc0YOt6-yc21rKDulTQ7O3GSMs"

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val systemPrompt = """
        You are a helpful city assistant for Bengaluru, India called "City Assistant". 
        You help residents with information about:
        - Civic issues like water shortages, power cuts, road problems, floods
        - Local areas in Bengaluru like Koramangala, Indiranagar, Whitefield, Jayanagar, Malleshwaram, HSR Layout, Electronic City, Hebbal, Rajajinagar
        - What to do during emergencies like floods or power cuts
        - Who to contact for civic complaints (BBMP, BESCOM, BWSSB)
        - General city information and tips
        Keep responses concise, helpful and friendly.
        If asked about something unrelated to the city or civic issues, politely redirect the conversation.
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvChat = findViewById(R.id.rvChat)
        chatAdapter = ChatAdapter(mutableListOf())
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // Welcome message
        chatAdapter.addMessage(
            ChatMessage(
                "👋 Hi! I'm your City Assistant for Bengaluru. Ask me anything about civic issues, local areas, or what to do during emergencies!",
                isUser = false
            )
        )

        val etChatInput = findViewById<EditText>(R.id.etChatInput)

        findViewById<Button>(R.id.btnSendChat).setOnClickListener {
            val userMessage = etChatInput.text.toString().trim()
            if (userMessage.isEmpty()) {
                Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add user message to chat
            chatAdapter.addMessage(ChatMessage(userMessage, isUser = true))
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)
            etChatInput.text.clear()

            // Add thinking indicator
            val thinkingMessage = ChatMessage("⏳ Thinking...", isUser = false)
            chatAdapter.addMessage(thinkingMessage)
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)

            // Call Gemini API
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = model.generateContent(
                        content {
                            text("$systemPrompt\n\nUser: $userMessage")
                        }
                    )
                    val botReply = response.text ?: "Sorry, I couldn't get a response."

                    withContext(Dispatchers.Main) {
                        // Remove thinking message and add real response
                        val lastIndex = chatAdapter.itemCount - 1
                        chatAdapter.messages.removeAt(lastIndex)
                        chatAdapter.notifyItemRemoved(lastIndex)
                        chatAdapter.addMessage(ChatMessage(botReply, isUser = false))
                        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val lastIndex = chatAdapter.itemCount - 1
                        chatAdapter.messages.removeAt(lastIndex)
                        chatAdapter.notifyItemRemoved(lastIndex)
                        chatAdapter.addMessage(
                            ChatMessage(
                                "Sorry, I couldn't connect. Please check your internet connection.",
                                isUser = false
                            )
                        )
                        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnBackChat).setOnClickListener { finish() }
    }
}