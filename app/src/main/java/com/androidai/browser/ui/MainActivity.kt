package com.androidai.browser.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidai.browser.data.AiLinksData
import com.androidai.browser.ui.theme.AndroidAiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContent {
                AndroidAiTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PremiumSwipeChat()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PremiumSwipeChat() {
    val context = LocalContext.current
    val chatModels = remember {
        try {
            AiLinksData.getDefaultLinks().filter { it.category == "Chatbots" }
        } catch (e: Exception) {
            Log.e("PremiumSwipeChat", "Error loading links: ${e.message}", e)
            emptyList()
        }
    }

    val pagerState = rememberPagerState(pageCount = { chatModels.size })
    val coroutineScope = rememberCoroutineScope()

    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
        )
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(tonalElevation = 4.dp) {
                    TopAppBar(
                        title = {
                            Text(
                                "Android AI",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
                if (chatModels.isNotEmpty()) {
                    Surface(color = Color.Transparent) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerGradient)
                        ) {
                            ScrollableTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                edgePadding = 12.dp,
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                chatModels.forEachIndexed { index, model ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                        },
                                        text = { Text(model.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (chatModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No chat models available.")
            }
        } else {
            val premiumGradient = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(premiumGradient)
            ) {
                HorizontalPager(
                    state = pagerState,
                ) { page ->
                    val model = chatModels[page]
                    ModelWebPage(
                        url = model.url,
                        onTitleChanged = { /* reserved */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelWebPage(
    url: String,
    onTitleChanged: (String) -> Unit
) {
    WebViewScreen(
        url = url,
        onBackPressed = { /* no-op inside pager */ }
    )
}

@Deprecated("Replaced by PremiumSwipeChat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidAiApp() { /* legacy kept for future */ }

@Composable
fun CategorySection(
    category: String,
    links: List<com.androidai.browser.data.AiLink>,
    onLinkClick: (String) -> Unit
) { /* legacy */ }
