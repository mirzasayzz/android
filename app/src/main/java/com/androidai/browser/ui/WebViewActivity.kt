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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.androidai.browser.ui.theme.AndroidAiTheme
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import android.webkit.ValueCallback
import android.webkit.URLUtil

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
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val initialUrl = remember(url) { if (url.startsWith("http")) url else "https://$url" }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val data = result.data
            val clip = data?.clipData
            val single = data?.data
            val uris: Array<Uri> = when {
                clip != null && clip.itemCount > 0 -> Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
                single != null -> arrayOf(single)
                else -> emptyArray()
            }
            filePathCallback?.onReceiveValue(if (uris.isNotEmpty()) uris else null)
        } catch (e: Exception) {
            Log.e("WebViewActivity", "File chooser result error: ${e.message}", e)
            filePathCallback?.onReceiveValue(null)
        } finally {
            filePathCallback = null
        }
    }

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
                    text = "Unable to load: $initialUrl",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { hasError = false; webViewRef.value?.loadUrl(initialUrl) }) {
                    Text("Retry")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AndroidView(
                    factory = { ctx ->
                        try {
                            WebView(ctx).apply {
                                webViewRef.value = this

                                // Cookies
                                CookieManager.getInstance().setAcceptCookie(true)
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                // WebViewClient (navigation and errors)
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        pageTitle = view?.title ?: url ?: "AI Tool"
                                        isLoading = false
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val uri = request?.url
                                        return if (uri != null && (uri.scheme == "http" || uri.scheme == "https")) {
                                            false
                                        } else if (uri != null) {
                                            try {
                                                ctx.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            } catch (_: Exception) {
                                            }
                                            true
                                        } else true
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

                                // WebChromeClient (progress, new windows, file chooser)
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        progress = newProgress
                                        isLoading = newProgress in 0..99
                                    }

                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: android.os.Message?
                                    ): Boolean {
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

                                    override fun onShowFileChooser(
                                        webView: WebView?,
                                        filePathCallbackParam: ValueCallback<Array<Uri>>?,
                                        fileChooserParams: FileChooserParams?
                                    ): Boolean {
                                        filePathCallback?.onReceiveValue(null)
                                        filePathCallback = filePathCallbackParam
                                        val contentIntent = try {
                                            fileChooserParams?.createIntent()
                                        } catch (_: Exception) {
                                            null
                                        }
                                        val intent = contentIntent ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                            type = "*/*"
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                        }
                                        return try {
                                            fileChooserLauncher.launch(intent)
                                            true
                                        } catch (e: Exception) {
                                            filePathCallback?.onReceiveValue(null)
                                            filePathCallback = null
                                            Log.e("WebViewActivity", "File chooser launch error: ${e.message}", e)
                                            false
                                        }
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
                                    mediaPlaybackRequiresUserGesture = false
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    // Mobile sizing
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                    // Always use a modern mobile user agent
                                    userAgentString = "Mozilla/5.0 (Linux; Android 11; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
                                }

                                // Force dark if supported
                                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                                    WebSettingsCompat.setForceDark(
                                        settings,
                                        if (isDark) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
                                    )
                                }

                                // Downloads
                                setDownloadListener { dlUrl, userAgent, contentDisposition, mimeType, _ ->
                                    try {
                                        val request = DownloadManager.Request(Uri.parse(dlUrl)).apply {
                                            setMimeType(mimeType)
                                            val cookies = CookieManager.getInstance().getCookie(dlUrl)
                                            if (cookies != null) addRequestHeader("cookie", cookies)
                                            if (!userAgent.isNullOrEmpty()) addRequestHeader("User-Agent", userAgent)
                                            setDescription("Downloading file")
                                            setTitle(URLUtil.guessFileName(dlUrl, contentDisposition, mimeType))
                                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                            setDestinationInExternalPublicDir(
                                                Environment.DIRECTORY_DOWNLOADS,
                                                URLUtil.guessFileName(dlUrl, contentDisposition, mimeType)
                                            )
                                        }
                                        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                        dm.enqueue(request)
                                        Toast.makeText(ctx, "Downloading...", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("WebViewActivity", "Download failed: ${e.message}", e)
                                        try {
                                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(dlUrl)))
                                        } catch (_: Exception) {
                                        }
                                    }
                                }

                                // Load initial URL
                                loadUrl(initialUrl)
                            }
                        } catch (e: Exception) {
                            Log.e("WebViewActivity", "Error creating WebView: ${e.message}", e)
                            hasError = true
                            WebView(ctx)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )

                // Bottom navigation controls overlay
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            IconButton(enabled = canGoBack, onClick = { webViewRef.value?.goBack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                            IconButton(enabled = canGoForward, onClick = { webViewRef.value?.goForward() }) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "Forward")
                            }
                            IconButton(onClick = { webViewRef.value?.reload() }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                            }
                        }
                        Row {
                            IconButton(onClick = { webViewRef.value?.loadUrl(initialUrl) }) {
                                Icon(Icons.Filled.Home, contentDescription = "Home")
                            }
                        }
                    }
                }
            }
        }
    }

    // Clean up WebView on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef.value?.apply {
                    stopLoading()
                    destroy()
                }
            } catch (_: Exception) {
            }
        }
    }

    if (showTopBar) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(pageTitle) },
                        navigationIcon = {
                            IconButton(onClick = onBackPressed) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                    if (isLoading) {
                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        ) { paddingValues -> content(paddingValues) }
    } else {
        content(PaddingValues(0.dp))
    }
}
 