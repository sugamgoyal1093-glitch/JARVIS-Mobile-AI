package com.ironman.jarvis
 
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.util.Locale
 
class MainActivity : ComponentActivity() {
 
    // 1. SET YOUR API KEY HERE
    private val apiKey = AQ.Ab8RN6LFi8xqviUsAtH9ZGLfkL5we3VcGQfvt7lVV1GwYZax4Q
    private lateinit var tts: TextToSpeech
    private lateinit var generativeModel: GenerativeModel
 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
 
        // Initialize Gemini AI
        generativeModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)
 
        // Initialize Text-to-Speech (JARVIS's voice)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setPitch(0.8f) // Make it sound a bit more like JARVIS
            }
        }
 
        setContent {
            JarvisUI()
        }
    }
 
    @Composable
    fun JarvisUI() {
        var responseText by remember { mutableStateOf("Ready for your command, Sir.") }
        var isListening by remember { mutableStateOf(false) }
 
        // Setup Voice Recognition Launcher
        val speechLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val userVoiceInput = data?.get(0) ?: ""
            if (userVoiceInput.isNotEmpty()) {
                responseText = "Thinking..."
                askJarvis(userVoiceInput) { aiReply ->
                    responseText = aiReply
                }
            }
            isListening = false
        }
 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0D17)), // Dark Space Blue
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // JARVIS Core (The Button)
            Button(
                onClick = {
                    isListening = true
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...")
                    }
                    speechLauncher.launch(intent)
                },
                modifier = Modifier.size(200.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)) // Glowing Cyan
            ) {
                Text(if (isListening) "Listening..." else "TAP TO SPEAK", color = Color.Black)
            }
 
            Spacer(modifier = Modifier.height(40.dp))
 
            // JARVIS Response Text
            Text(
                text = responseText,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
 
    private fun askJarvis(prompt: String, onResult: (String) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                val reply = response.text ?: "I encountered an error, Sir."
                onResult(reply)
                // Make JARVIS speak the reply
                tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, null)
            } catch (e: Exception) {
                onResult("Connection failed, Sir.")
            }
        }
    }
 
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

 