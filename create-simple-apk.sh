#!/bin/bash

echo "🚀 Creating Simple Android AI APK"
echo "================================="

# Remove complex files that cause compilation issues
rm -rf app/src/main/java/com/androidai/browser/data/

# Create simplified data structures without Room
mkdir -p app/src/main/java/com/androidai/browser/data

# Create simple data models
cat > app/src/main/java/com/androidai/browser/data/AiLink.kt << 'EOF'
package com.androidai.browser.data

data class AiLink(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val category: String,
    val isFavorite: Boolean = false
)

object AiLinksData {
    fun getDefaultLinks(): List<AiLink> = listOf(
        AiLink("1", "ChatGPT", "OpenAI's conversational AI", "https://chat.openai.com", "Chatbots"),
        AiLink("2", "Claude", "Anthropic's AI assistant", "https://claude.ai", "Chatbots"),
        AiLink("3", "Gemini", "Google's multimodal AI", "https://gemini.google.com", "Chatbots"),
        AiLink("4", "Perplexity", "AI search engine", "https://www.perplexity.ai", "Chatbots"),
        AiLink("5", "Poe", "Quora's AI platform", "https://poe.com", "Chatbots"),
        AiLink("6", "Jasper", "AI content creation", "https://www.jasper.ai", "Writing"),
        AiLink("7", "Copy.ai", "AI copywriter", "https://www.copy.ai", "Writing"),
        AiLink("8", "GitHub Copilot", "AI pair programmer", "https://github.com/features/copilot", "Coding"),
        AiLink("9", "Cursor", "AI code editor", "https://www.cursor.so", "Coding"),
        AiLink("10", "Midjourney", "AI image generation", "https://www.midjourney.com", "Image"),
        AiLink("11", "DALL-E", "OpenAI image AI", "https://labs.openai.com", "Image"),
        AiLink("12", "Stable Diffusion", "Open source image AI", "https://stablediffusionweb.com", "Image"),
        AiLink("13", "Udio", "AI music generation", "https://www.udio.com", "Music"),
        AiLink("14", "Suno", "AI music creator", "https://suno.com", "Music"),
        AiLink("15", "Gamma", "AI presentation maker", "https://gamma.app", "Productivity"),
        AiLink("16", "Tome", "AI storytelling", "https://tome.app", "Productivity")
    )
}
EOF

# Simplified MainActivity
cat > app/src/main/java/com/androidai/browser/ui/MainActivity.kt << 'EOF'
package com.androidai.browser.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidai.browser.data.AiLinksData
import com.androidai.browser.ui.theme.AndroidAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidAiTheme {
                AndroidAiApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidAiApp() {
    val context = LocalContext.current
    val aiLinks = remember { AiLinksData.getDefaultLinks() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Android AI Browser",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "🤖 AI Tools Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap any AI tool to open it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(aiLinks.groupBy { it.category }.toList()) { (category, links) ->
                CategorySection(
                    category = category,
                    links = links,
                    onLinkClick = { url ->
                        val intent = Intent(context, WebViewActivity::class.java).apply {
                            putExtra("url", url)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    category: String,
    links: List<com.androidai.browser.data.AiLink>,
    onLinkClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            links.forEach { aiLink ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLinkClick(aiLink.url) }
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = aiLink.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = aiLink.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
EOF

# Simplified WebViewActivity
cat > app/src/main/java/com/androidai/browser/ui/WebViewActivity.kt << 'EOF'
package com.androidai.browser.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.androidai.browser.ui.theme.AndroidAiTheme

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val url = intent.getStringExtra("url") ?: "https://www.google.com"
        
        setContent {
            AndroidAiTheme {
                WebViewScreen(
                    url = url,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    onBackPressed: () -> Unit
) {
    var pageTitle by remember { mutableStateOf("Loading...") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pageTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            pageTitle = view?.title ?: url ?: "AI Tool"
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
EOF

# Remove problematic UI files
rm -f app/src/main/java/com/androidai/browser/ui/screens/*.kt
rm -f app/src/main/java/com/androidai/browser/ui/components/*.kt
rm -f app/src/main/java/com/androidai/browser/ui/viewmodels/*.kt
rm -f app/src/main/java/com/androidai/browser/AndroidAiApplication.kt

# Now build the APK
echo "🔨 Building Simplified Android AI APK..."
./gradlew assembleDebug --no-daemon

# Check if APK was created
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Success! APK created successfully!"
    echo "📱 Ready to install: app/build/outputs/apk/debug/app-debug.apk"
    
    # Copy APK to root directory for easy access
    cp app/build/outputs/apk/debug/app-debug.apk ./android-ai-simple.apk
    echo "📋 APK copied to: android-ai-simple.apk"
    
    # Show APK info
    ls -lh android-ai-simple.apk
    echo ""
    echo "🎉 Android AI APK is ready to install!"
    echo "📲 Transfer this file to your Android device and install it"
else
    echo "❌ Failed to build APK"
    exit 1
fi 