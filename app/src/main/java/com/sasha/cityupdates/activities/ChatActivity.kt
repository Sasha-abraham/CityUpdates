package com.sasha.cityupdates.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sasha.cityupdates.R
import com.sasha.cityupdates.adapters.ChatAdapter
import com.sasha.cityupdates.models.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvChat: RecyclerView

    // Replace with your HuggingFace token
    private val apiKey = "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"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """You are a helpful city assistant for Bengaluru, India. 
You help residents with civic issues like water shortages, power cuts, road problems, floods, 
and local area information for Koramangala, Indiranagar, Whitefield, Jayanagar, Malleshwaram, 
HSR Layout, Electronic City, Hebbal, and Rajajinagar. 
Keep responses short, friendly and helpful. Answer in 2-3 sentences maximum."""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        rvChat = findViewById(R.id.rvChat)
        chatAdapter = ChatAdapter(mutableListOf())
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvChat.layoutManager = layoutManager
        rvChat.adapter = chatAdapter

        chatAdapter.addMessage(
            ChatMessage(
                "👋 Hi! I'm your City Assistant for Bengaluru. Ask me anything about civic issues, local areas, or what to do during emergencies!",
                isUser = false
            )
        )

        val etChatInput = findViewById<EditText>(R.id.etChatInput)
        val btnSend = findViewById<Button>(R.id.btnSendChat)

        btnSend.setOnClickListener {
            val userMessage = etChatInput.text.toString().trim()
            if (userMessage.isEmpty()) {
                Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            chatAdapter.addMessage(ChatMessage(userMessage, isUser = true))
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)
            etChatInput.text.clear()
            btnSend.isEnabled = false

            chatAdapter.addMessage(ChatMessage("⏳ Thinking...", isUser = false))
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prompt = "You are a Bengaluru city assistant. Answer this question helpfully and briefly: $userMessage"

                    val jsonBody = JSONObject().apply {
                        put("inputs", prompt)
                        put("parameters", JSONObject().apply {
                            put("max_new_tokens", 150)
                            put("temperature", 0.7)
                        })
                    }

                    val requestBody = jsonBody.toString()
                        .toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url("https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    val botReply = if (response.isSuccessful) {
                        try {
                            val generatedText = try {
                                JSONArray(responseBody).getJSONObject(0).getString("generated_text").trim()
                            } catch (e: Exception) {
                                try {
                                    JSONObject(responseBody).getString("generated_text").trim()
                                } catch (e2: Exception) {
                                    "I'm having trouble responding. Please try again!"
                                }
                            }
                            if (generatedText.isEmpty()) "I'm not sure about that. Try asking something else!"
                            else generatedText
                        } catch (e: Exception) {
                            "I received a response but couldn't read it. Please try again!"
                        }
                    } else {
                        "Sorry, the service is busy right now. Please try again in a moment!"
                    }

                    withContext(Dispatchers.Main) {
                        val lastIndex = chatAdapter.itemCount - 1
                        chatAdapter.messages.removeAt(lastIndex)
                        chatAdapter.notifyItemRemoved(lastIndex)
                        chatAdapter.addMessage(ChatMessage(botReply, isUser = false))
                        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                        btnSend.isEnabled = true
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val lastIndex = chatAdapter.itemCount - 1
                        chatAdapter.messages.removeAt(lastIndex)
                        chatAdapter.notifyItemRemoved(lastIndex)
                        chatAdapter.addMessage(
                            ChatMessage(
                                "Connection error. Please check your internet and try again.",
                                isUser = false
                            )
                        )
                        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                        btnSend.isEnabled = true
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnBackChat).setOnClickListener { finish() }
    }
}