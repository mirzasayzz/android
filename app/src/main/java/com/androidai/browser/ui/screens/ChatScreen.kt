package com.androidai.browser.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidai.browser.data.AiLinksData
import com.androidai.browser.ui.WebViewScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen() {
    val chatModels = remember {
        try {
            AiLinksData.getDefaultLinks().filter { it.category == "Chatbots" }
        } catch (e: Exception) {
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
                    WebViewScreen(
                        url = model.url,
                        onBackPressed = {},
                        showTopBar = false
                    )
                }
            }
        }
    }
} 