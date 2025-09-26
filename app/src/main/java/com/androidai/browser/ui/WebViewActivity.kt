package com.androidai.browser.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
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
import androidx.compose.ui.unit.dp

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val url = intent.getStringExtra("url") ?: "https://www.google.com"
            
            setContent {
                AndroidAiTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        WebViewScreen(
                            url = url,
                            onBackPressed = { finish() },
                            showTopBar = true
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WebViewActivity", "Error in onCreate: ${e.message}", e)
            finish()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    onBackPressed: () -> Unit,
    showTopBar: Boolean = false
) {
    var pageTitle by remember { mutableStateOf("Loading...") }
    var hasError by remember { mutableStateOf(false) }
    
    val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
        if (hasError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Error loading page",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unable to load: $url",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { hasError = false }
                ) {
                    Text("Retry")
                }
            }
        } else {
            AndroidView(
                factory = { context ->
                    try {
                        WebView(context).apply {
                            // Cookies
                            CookieManager.getInstance().setAcceptCookie(true)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            // Clients
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    pageTitle = view?.title ?: url ?: "AI Tool"
                                }
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val uri = request?.url
                                    return if (uri != null && (uri.scheme == "http" || uri.scheme == "https")) {
                                        false
                                    } else {
                                        // Let the system handle non-http(s) schemes
                                        true
                                    }
                                }
                                
                                override fun onReceivedError(
                                    view: WebView?,
                                    errorCode: Int,
                                    description: String?,
                                    failingUrl: String?
                                ) {
                                    super.onReceivedError(view, errorCode, description, failingUrl)
                                    Log.e("WebViewActivity", "WebView error: $description")
                                    hasError = true
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onCreateWindow(
                                    view: WebView?,
                                    isDialog: Boolean,
                                    isUserGesture: Boolean,
                                    resultMsg: android.os.Message?
                                ): Boolean {
                                    // Open new windows in the same WebView
                                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                                    val temp = WebView(view!!.context)
                                    temp.webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            v: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val target = request?.url.toString()
                                            view.loadUrl(target)
                                            return true
                                        }
                                    }
                                    transport?.webView = temp
                                    resultMsg?.sendToTarget()
                                    return true
                                }
                            }
                            // Settings
                            settings.apply {
                                javaScriptEnabled = true
                                javaScriptCanOpenWindowsAutomatically = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                setSupportMultipleWindows(true)
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                                // Mobile sizing
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                // Per-site UA tweaks
                                val t = if (url.startsWith("http")) url else "https://$url"
                                userAgentString = if (t.contains("perplexity.ai")) {
                                    // Use desktop UA to avoid aggressive app banner gate
                                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                                } else {
                                    "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
                                }
                            }
                            val targetUrl = if (url.startsWith("http")) url else "https://$url"
                            loadUrl(targetUrl)
                        }
                    } catch (e: Exception) {
                        Log.e("WebViewActivity", "Error creating WebView: ${e.message}", e)
                        hasError = true
                        WebView(context)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }

    if (showTopBar) {
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
        ) { paddingValues -> content(paddingValues) }
    } else {
        content(PaddingValues(0.dp))
    }
}
