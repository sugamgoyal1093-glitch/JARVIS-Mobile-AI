dependencies {
    // UI Tools
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
 
    // JARVIS AI Brain (Google Gemini)
    implementation("google.ai.client.generativeai:generativeai:0.7.0")
    
    // Background Tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
 
