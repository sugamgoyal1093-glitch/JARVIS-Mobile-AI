MainActivity.kt
package com.ironman.jarvis
 
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.util.*
 
class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    
    // JARVIS BRAIN KEY
    private val apiKey = "PASTE_YOUR_GEMINI_KEY_HERE"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
 
        setContent {
            val scope = rememberCoroutineScope()
            var statusText by remember { mutableStateOf("JARVIS IS ONLINE") }
            var isThinking by remember { mutableStateOf(false) }
 
            val speechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val userSpeech = data?.get(0) ?: ""
                
                if (userSpeech.isNotEmpty()) {
                    statusText = "Analyzing: $userSpeech"
                    isThinking = true
                    scope.launch {
                        val response = askJarvisAI(userSpeech)
                        isThinking = false
                        statusText = response
                        speak(response)
                        executeJarvisAction(response)
                    }
                }
            }
 
            // JARVIS UI
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000B14)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SYSTEM: ACTIVE", color = Color(0xFF00E5FF), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(40.dp))
 
                    // Arc Reactor Button
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(160.dp).clickable {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                                }
                                speechLauncher.launch(intent)
                            },
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, Color(0xFF00E5FF))
                        ) {
                            Text("TAP TO COMMAND", color = Color(0xFF00E5FF), modifier = Modifier.wrapContentSize())
                        }
                    }
 
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(statusText, color = Color.White, modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
 
    private suspend fun askJarvisAI(userInput: String): String {
        val model = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)
        val prompt = "You are JARVIS. User says: $userInput. Respond in Hinglish. If they want to Open an app, Call, or Check Battery, add [ACTION:TYPE:NAME] at the end."
        return try { model.generateContent(prompt).text ?: "Sir?" } catch (e: Exception) { "Offline, Sir." }
    }
 
    private fun executeJarvisAction(aiResponse: String) {
        when {
            aiResponse.contains("[ACTION:OPEN:YOUTUBE]") -> openApp("com.google.android.youtube")
            aiResponse.contains("[ACTION:OPEN:WHATSAPP]") -> openApp("com.whatsapp")
            aiResponse.contains("[ACTION:CALL:") -> {
                val intent = Intent(Intent.ACTION_DIAL) // Phase 2: Opens dialer for safety
                startActivity(intent)
            }
            aiResponse.contains("[ACTION:BATTERY]") -> {
                val bm = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
                val level = bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
                speak("Sir, the battery is at $level percent.")
            }
        }
    }
 
    private fun openApp(pkg: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) startActivity(intent) else speak("App not found, Sir.")
    }
 
    private fun speak(text: String) {
        val clean = text.replace(Regex("\\[.*?\\]"), "")
        tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, null)
    }
 
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale("hi", "IN") }
}
 
