package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.local.DownloadHistoryDatabase
import com.example.data.local.DownloadItemEntity
import com.example.data.model.ActiveDownloadProgress
import com.example.data.model.QualityStreamOption
import com.example.data.model.VideoQualityTier
import com.example.data.network.MediaDownloadEngine
import com.example.data.network.TikTokExtractorRepository
import com.example.data.preferences.StudioPreferencesRepository
import com.example.data.preferences.StudioSettings
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioElevatedSurface
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TikStudioTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FloatingOverlayUiState(
    val isExpanded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgressPct: Int = 0,
    val currentStatusMessage: String = "Sẵn sàng",
    val lastDownloadedTitle: String? = null,
    val autoScrollSeconds: Int = 10,
    val autoSwipeAfterDownload: Boolean = true,
    val isAutoScrollActive: Boolean = false,
    val countdownRemaining: Int = 10
)

class FloatingTikTokShortcutService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private val extractorRepository by lazy { TikTokExtractorRepository() }
    private val database by lazy { DownloadHistoryDatabase.getInstance(applicationContext) }
    private val downloadEngine by lazy { MediaDownloadEngine(applicationContext, database.downloadHistoryDao()) }
    private val preferencesRepository by lazy { StudioPreferencesRepository(applicationContext) }

    private val _overlayState = MutableStateFlow(FloatingOverlayUiState())
    val overlayState = _overlayState.asStateFlow()

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val lifecycleOwner = CustomServiceLifecycleOwner()

    companion object {
        private const val TAG = "FloatingShortcutService"
        const val NOTIFICATION_ID = 2026
        const val CHANNEL_ID = "tikstudio_floating_shortcut_channel"

        const val ACTION_START = "com.example.service.action.START_SHORTCUT"
        const val ACTION_STOP = "com.example.service.action.STOP_SHORTCUT"
        const val ACTION_TRIGGER_DOWNLOAD_NEXT = "com.example.service.action.TRIGGER_DOWNLOAD_NEXT"

        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, FloatingTikTokShortcutService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingTikTokShortcutService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun triggerDownloadAndNext(context: Context) {
            val intent = Intent(context, FloatingTikTokShortcutService::class.java).apply {
                action = ACTION_TRIGGER_DOWNLOAD_NEXT
            }
            context.startService(intent)
        }

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        isRunning = true

        // Collect auto scroll state from Accessibility service
        serviceScope.launch {
            TikAutoScrollAccessibilityService.autoScrollState.collect { autoState ->
                _overlayState.update {
                    it.copy(
                        isAutoScrollActive = autoState.isAutoScrolling,
                        countdownRemaining = autoState.countdownRemaining,
                        autoScrollSeconds = autoState.intervalSeconds
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TRIGGER_DOWNLOAD_NEXT -> {
                handleDownloadAndNextAction()
            }
            else -> {
                if (canDrawOverlays(this)) {
                    showFloatingOverlayView()
                } else {
                    Log.w(TAG, "Cannot draw overlays: permission not granted")
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingOverlayView()
        lifecycleOwner.onDestroy()
        serviceScope.cancel()
        isRunning = false
    }

    private fun showFloatingOverlayView() {
        if (overlayComposeView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 300
        }
        windowLayoutParams = params

        overlayComposeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                TikStudioTheme {
                    FloatingOverlayWidget(
                        uiStateFlow = _overlayState,
                        onToggleExpand = {
                            _overlayState.update { it.copy(isExpanded = !it.isExpanded) }
                        },
                        onDownloadAndNext = {
                            handleDownloadAndNextAction()
                        },
                        onSwipeNextOnly = {
                            handleSwipeNextOnly()
                        },
                        onSwipePreviousOnly = {
                            TikAutoScrollAccessibilityService.swipeToPreviousVideo()
                        },
                        onToggleAutoScroll = {
                            handleToggleAutoScroll()
                        },
                        onOpenMainApp = {
                            openMainApp()
                        },
                        onCloseOverlay = {
                            stopSelf()
                        },
                        onDragOffset = { dx, dy ->
                            windowLayoutParams?.let { lp ->
                                lp.x += dx.toInt()
                                lp.y += dy.toInt()
                                try {
                                    windowManager?.updateViewLayout(this@apply, lp)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error updating overlay layout", e)
                                }
                            }
                        }
                    )
                }
            }
        }

        try {
            windowManager?.addView(overlayComposeView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding overlay view to WindowManager", e)
        }
    }

    private fun removeFloatingOverlayView() {
        if (overlayComposeView != null && windowManager != null) {
            try {
                windowManager?.removeView(overlayComposeView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view", e)
            }
            overlayComposeView = null
        }
    }

    private fun handleDownloadAndNextAction() {
        serviceScope.launch {
            val clipText = readClipboardDirect()
            val cleanUrl = extractorRepository.extractTikTokUrlFromClipboard(clipText)
                ?: extractorRepository.extractCleanUrl(clipText.orEmpty())

            if (cleanUrl.isBlank() || (!cleanUrl.contains("tiktok.com", ignoreCase = true) && !cleanUrl.contains("douyin.com", ignoreCase = true))) {
                _overlayState.update {
                    it.copy(currentStatusMessage = "Hãy sao chép link TikTok trước khi bấm tải!")
                }
                // Still swipe next if user tapped Next
                delay(800)
                TikAutoScrollAccessibilityService.swipeToNextVideo()
                return@launch
            }

            _overlayState.update {
                it.copy(
                    isDownloading = true,
                    downloadProgressPct = 5,
                    currentStatusMessage = "Đang phân tích link TikTok..."
                )
            }

            try {
                val media = extractorRepository.inspectTikTokUrl(cleanUrl).getOrNull()
                if (media == null) {
                    _overlayState.update {
                        it.copy(
                            isDownloading = false,
                            currentStatusMessage = "Không phân tích được video. Đang chuyển tiếp..."
                        )
                    }
                    delay(500)
                    TikAutoScrollAccessibilityService.swipeToNextVideo()
                    return@launch
                }

                _overlayState.update {
                    it.copy(
                        downloadProgressPct = 25,
                        currentStatusMessage = "Đang tải video Full HD không logo..."
                    )
                }

                val currentSettings = preferencesRepository.settingsFlow.first()
                val targetStream = media.streams.firstOrNull { it.tier == currentSettings.defaultQualityTier }
                    ?: media.streams.firstOrNull { it.tier == VideoQualityTier.FULL_HD_1080P }
                    ?: media.streams.firstOrNull()
                    ?: QualityStreamOption(
                        tier = VideoQualityTier.FULL_HD_1080P,
                        streamUrl = media.originalUrl,
                        fileSizeBytes = 12_000_000L,
                        bitrateKbps = 2400,
                        fps = 60,
                        codecLabel = "H.264 / AVC"
                    )

                downloadEngine.downloadVideoOrAudio(
                    media = media,
                    stream = targetStream,
                    settings = currentSettings,
                    customPrefix = "",
                    onProgress = { progress ->
                        _overlayState.update {
                            it.copy(
                                downloadProgressPct = (progress.progress * 100).toInt().coerceIn(10, 99),
                                currentStatusMessage = "Đang tải: ${(progress.progress * 100).toInt()}%"
                            )
                        }
                    }
                )

                _overlayState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgressPct = 100,
                        currentStatusMessage = "✅ Đã tải xong video! Đang chuyển tiếp...",
                        lastDownloadedTitle = media.title.take(35)
                    )
                }

                // Automatically swipe to the next TikTok video!
                delay(500)
                TikAutoScrollAccessibilityService.swipeToNextVideo()

            } catch (e: Exception) {
                Log.e(TAG, "Download and next error", e)
                _overlayState.update {
                    it.copy(
                        isDownloading = false,
                        currentStatusMessage = "Tải thất bại. Đang chuyển video..."
                    )
                }
                delay(500)
                TikAutoScrollAccessibilityService.swipeToNextVideo()
            }
        }
    }

    private fun handleSwipeNextOnly() {
        val success = TikAutoScrollAccessibilityService.swipeToNextVideo()
        _overlayState.update {
            it.copy(
                currentStatusMessage = if (success) "Đã chuyển sang video tiếp theo" else "Hãy bật quyền Trợ năng để tự động chuyển!"
            )
        }
    }

    private fun handleToggleAutoScroll() {
        val current = _overlayState.value.isAutoScrollActive
        if (current) {
            TikAutoScrollAccessibilityService.stopHandsFreeAutoScroll()
        } else {
            TikAutoScrollAccessibilityService.startHandsFreeAutoScroll(_overlayState.value.autoScrollSeconds)
        }
    }

    private fun openMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun readClipboardDirect(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
        val primaryClip = clipboard.primaryClip ?: return null
        if (primaryClip.itemCount > 0) {
            val text = primaryClip.getItemAt(0)?.coerceToText(this)?.toString()
            return text
        }
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.floating_service_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.floating_service_notification_text)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.floating_service_notification_title))
            .setContentText(getString(R.string.floating_service_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

/**
 * Service lifecycle owner to safely run Compose in WindowManager
 */
private class CustomServiceLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

/**
 * Floating Overlay UI Component
 */
@Composable
fun FloatingOverlayWidget(
    uiStateFlow: StateFlow<FloatingOverlayUiState>,
    onToggleExpand: () -> Unit,
    onDownloadAndNext: () -> Unit,
    onSwipeNextOnly: () -> Unit,
    onSwipePreviousOnly: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onOpenMainApp: () -> Unit,
    onCloseOverlay: () -> Unit,
    onDragOffset: (Float, Float) -> Unit
) {
    val state by uiStateFlow.collectAsState()

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragOffset(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        if (!state.isExpanded) {
            // Collapsed Compact Floating Pill
            CollapsedShortcutBadge(
                isDownloading = state.isDownloading,
                isAutoScrolling = state.isAutoScrollActive,
                countdown = state.countdownRemaining,
                onClick = onToggleExpand
            )
        } else {
            // Expanded Control Panel over TikTok
            ExpandedShortcutPanel(
                state = state,
                onCollapse = onToggleExpand,
                onDownloadAndNext = onDownloadAndNext,
                onSwipeNextOnly = onSwipeNextOnly,
                onSwipePreviousOnly = onSwipePreviousOnly,
                onToggleAutoScroll = onToggleAutoScroll,
                onOpenMainApp = onOpenMainApp,
                onCloseOverlay = onCloseOverlay
            )
        }
    }
}

@Composable
private fun CollapsedShortcutBadge(
    isDownloading: Boolean,
    isAutoScrolling: Boolean,
    countdown: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xEE111827),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isAutoScrolling) EmeraldBadge else if (isDownloading) GoldBadge else ElectricCyan
        ),
        shadowElevation = 8.dp,
        modifier = Modifier
            .size(54.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(46.dp),
                    color = GoldBadge,
                    strokeWidth = 3.dp
                )
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Đang tải",
                    tint = GoldBadge,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isAutoScrolling) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Tự động lướt",
                        tint = EmeraldBadge,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${countdown}s",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldBadge
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Phím tắt tải TikTok",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "TIK",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedShortcutPanel(
    state: FloatingOverlayUiState,
    onCollapse: () -> Unit,
    onDownloadAndNext: () -> Unit,
    onSwipeNextOnly: () -> Unit,
    onSwipePreviousOnly: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onOpenMainApp: () -> Unit,
    onCloseOverlay: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xF20B132B),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
        shadowElevation = 16.dp,
        modifier = Modifier
            .widthIn(min = 260.dp, max = 290.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp))
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "TikStudio HD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Thu gọn",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onCloseOverlay,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Đóng",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C2541))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = state.currentStatusMessage,
                    fontSize = 11.sp,
                    color = if (state.isDownloading) GoldBadge else if (state.isAutoScrollActive) EmeraldBadge else ElectricCyan,
                    maxLines = 2
                )
            }

            // PRIMARY ACTION 1: Tải & Chuyển video tự động
            Surface(
                onClick = onDownloadAndNext,
                shape = RoundedCornerShape(12.dp),
                color = ElectricCyan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF001F24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚡ Tải & Chuyển Video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF001F24)
                    )
                }
            }

            // ACTION 2 & 3: Lướt tự động + Chuyển nhanh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Auto Scroll Toggle
                Surface(
                    onClick = onToggleAutoScroll,
                    shape = RoundedCornerShape(10.dp),
                    color = if (state.isAutoScrollActive) EmeraldBadge.copy(alpha = 0.2f) else StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.isAutoScrollActive) EmeraldBadge else StudioCardBorder
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (state.isAutoScrollActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = if (state.isAutoScrollActive) EmeraldBadge else TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isAutoScrollActive) "Dừng (${state.countdownRemaining}s)" else "Lướt tự động",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.isAutoScrollActive) EmeraldBadge else TextPrimary
                        )
                    }
                }

                // Next Video Direct
                Surface(
                    onClick = onSwipeNextOnly,
                    shape = RoundedCornerShape(10.dp),
                    color = StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chuyển tiếp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricCyan
                        )
                    }
                }
            }

            // Bottom Jump to App
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMainApp() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mở TikStudio HD xem kho tải",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
