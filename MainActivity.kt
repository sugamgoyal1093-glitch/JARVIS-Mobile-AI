package com.ironman.jarvis // Make sure this matches your actual package name at the top of your file
 
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
 
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize the AI Brain (Gemini)
        val jarvisBrain = GenerativeModel(
            modelName = "gemini-1.5-flash",
            // PASTE YOUR API KEY FROM YOUR local.properties HERE
            apiKey = Ab8RN6LFi8xqviUsAtH9ZGLfkL5we3VcGQfvt7lVV1GwYZax4Q 
            systemInstruction = content { text("You are JARVIS, the highly advanced AI assistant created by Tony Stark. Be polite, witty, and efficient. Use phrases like 'At your service, sir' or 'Ready when you are'.") }
        )
 
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                JarvisChatScreen(jarvisBrain)
            }
        }
    }
}
 
@Composable
fun JarvisChatScreen(model: GenerativeModel) {
    val scope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<Pair<String, String>>() } // User to JARVIS
 
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Chat History Area
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(chatMessages) { message ->
                Text(text = "You: ${message.first}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "JARVIS: ${message.second}", color = MaterialTheme.colorScheme.primary)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
 
        // Input Area
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask JARVIS...") }
            )
            Button(onClick = {
                if (userInput.isNotBlank()) {
                    val currentInput = userInput
                    userInput = ""
                    scope.launch {
                        try {
                            val response = model.generateContent(currentInput)
                            chatMessages.add(currentInput to (response.text ?: "I'm having trouble connecting, sir."))
                        } catch (e: Exception) {
                            chatMessages.add(currentInput to "Systems are offline, sir. Check your connection.")
                        }
                    }
                }
            }) {
                Text("Send")
            }
        }
    }
}
