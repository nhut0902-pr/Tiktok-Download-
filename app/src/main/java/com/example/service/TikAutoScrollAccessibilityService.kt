package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

data class AutoScrollState(
    val isAutoScrolling: Boolean = false,
    val intervalSeconds: Int = 10,
    val countdownRemaining: Int = 10,
    val totalSwipesCompleted: Int = 0,
    val lastActionMessage: String = "Sẵn sàng"
)

class TikAutoScrollAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var autoScrollJob: Job? = null

    companion object {
        private const val TAG = "TikAutoScrollService"

        private var instanceRef: WeakReference<TikAutoScrollAccessibilityService>? = null

        private val _autoScrollState = MutableStateFlow(AutoScrollState())
        val autoScrollState: StateFlow<AutoScrollState> = _autoScrollState.asStateFlow()

        val isServiceActive: Boolean
            get() = instanceRef?.get() != null

        /**
         * Checks if the Accessibility Service is enabled in Android System Settings.
         */
        fun isAccessibilitySettingsEnabled(context: Context): Boolean {
            val expectedComponentName = "${context.packageName}/${TikAutoScrollAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.split(":").any { it.equals(expectedComponentName, ignoreCase = true) }
        }

        /**
         * Dispatches a gesture to swipe up and transition automatically to the next TikTok video.
         */
        fun swipeToNextVideo(delayMs: Long = 0, onComplete: ((Boolean) -> Unit)? = null): Boolean {
            val service = instanceRef?.get() ?: return false
            return service.performSwipeUp(delayMs, onComplete)
        }

        /**
         * Dispatches a gesture to swipe down to the previous video.
         */
        fun swipeToPreviousVideo(onComplete: ((Boolean) -> Unit)? = null): Boolean {
            val service = instanceRef?.get() ?: return false
            return service.performSwipeDown(onComplete)
        }

        /**
         * Starts the hands-free automated video scroller.
         */
        fun startHandsFreeAutoScroll(intervalSeconds: Int = 10) {
            val service = instanceRef?.get()
            if (service != null) {
                service.startAutoScroll(intervalSeconds)
            } else {
                _autoScrollState.update {
                    it.copy(
                        isAutoScrolling = true,
                        intervalSeconds = intervalSeconds,
                        countdownRemaining = intervalSeconds,
                        lastActionMessage = "Đang chờ kích hoạt quyền Trợ năng"
                    )
                }
            }
        }

        /**
         * Stops the hands-free automated video scroller.
         */
        fun stopHandsFreeAutoScroll() {
            instanceRef?.get()?.stopAutoScroll() ?: run {
                _autoScrollState.update {
                    it.copy(isAutoScrolling = false, lastActionMessage = "Đã dừng tự động lướt")
                }
            }
        }

        fun toggleHandsFreeAutoScroll(intervalSeconds: Int = 10) {
            if (_autoScrollState.value.isAutoScrolling) {
                stopHandsFreeAutoScroll()
            } else {
                startHandsFreeAutoScroll(intervalSeconds)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
        Log.d(TAG, "TikAutoScrollAccessibilityService connected successfully")
        _autoScrollState.update { it.copy(lastActionMessage = "Dịch vụ tự động chuyển video đã sẵn sàng") }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Can be used to monitor TikTok screen state transitions
    }

    override fun onInterrupt() {
        stopAutoScroll()
        Log.d(TAG, "TikAutoScrollAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoScroll()
        serviceScope.cancel()
        if (instanceRef?.get() == this) {
            instanceRef = null
        }
    }

    /**
     * Executes an upward swipe gesture (from bottom to top) which advances TikTok to the next video.
     */
    fun performSwipeUp(delayMs: Long = 0, onComplete: ((Boolean) -> Unit)? = null): Boolean {
        serviceScope.launch {
            if (delayMs > 0) {
                delay(delayMs)
            }
            executeSwipe(isUp = true, onComplete = onComplete)
        }
        return true
    }

    /**
     * Executes a downward swipe gesture (from top to bottom) which returns to the previous TikTok video.
     */
    fun performSwipeDown(onComplete: ((Boolean) -> Unit)? = null): Boolean {
        serviceScope.launch {
            executeSwipe(isUp = false, onComplete = onComplete)
        }
        return true
    }

    private fun executeSwipe(isUp: Boolean, onComplete: ((Boolean) -> Unit)?) {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat().coerceAtLeast(720f)
        val height = displayMetrics.heightPixels.toFloat().coerceAtLeast(1280f)

        val centerX = width * 0.5f
        val startY = if (isUp) height * 0.76f else height * 0.24f
        val endY = if (isUp) height * 0.22f else height * 0.78f

        val swipePath = Path().apply {
            moveTo(centerX, startY)
            lineTo(centerX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(swipePath, 0, 260)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        try {
            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    _autoScrollState.update {
                        it.copy(
                            totalSwipesCompleted = it.totalSwipesCompleted + 1,
                            lastActionMessage = if (isUp) "Đã chuyển sang video tiếp theo" else "Đã quay lại video trước"
                        )
                    }
                    onComplete?.invoke(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    _autoScrollState.update {
                        it.copy(lastActionMessage = "Thao tác vuốt bị huỷ")
                    }
                    onComplete?.invoke(false)
                }
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch gesture", e)
            onComplete?.invoke(false)
        }
    }

    private fun startAutoScroll(intervalSec: Int) {
        val safeInterval = intervalSec.coerceIn(3, 60)
        autoScrollJob?.cancel()

        _autoScrollState.update {
            it.copy(
                isAutoScrolling = true,
                intervalSeconds = safeInterval,
                countdownRemaining = safeInterval,
                lastActionMessage = "Đang lướt video tự động (${safeInterval}s)"
            )
        }

        autoScrollJob = serviceScope.launch {
            while (isActive) {
                for (remaining in safeInterval downTo 1) {
                    _autoScrollState.update { it.copy(countdownRemaining = remaining) }
                    delay(1000)
                }
                executeSwipe(isUp = true, onComplete = null)
                // Small breather after swipe before restarting countdown
                delay(800)
            }
        }
    }

    private fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
        _autoScrollState.update {
            it.copy(
                isAutoScrolling = false,
                lastActionMessage = "Đã dừng tự động lướt video"
            )
        }
    }
}
