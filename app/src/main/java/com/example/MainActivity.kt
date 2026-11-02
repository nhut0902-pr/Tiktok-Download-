package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.data.local.DownloadHistoryDatabase
import com.example.data.network.MediaDownloadEngine
import com.example.data.network.TikTokExtractorRepository
import com.example.data.preferences.StudioPreferencesRepository
import com.example.data.update.GitHubAutoUpdater
import com.example.service.ScreenRecorderForegroundService
import com.example.ui.screens.TikStudioMainScreen
import com.example.ui.screens.readClipboardText
import com.example.ui.theme.TikStudioTheme
import com.example.ui.viewmodel.TikStudioViewModel

class MainActivity : ComponentActivity() {

    private val extractorRepository by lazy { TikTokExtractorRepository() }
    private val database by lazy { DownloadHistoryDatabase.getInstance(applicationContext) }
    private val downloadEngine by lazy {
        MediaDownloadEngine(applicationContext, database.downloadHistoryDao())
    }
    private val preferencesRepository by lazy {
        StudioPreferencesRepository(applicationContext)
    }
    private val gitHubAutoUpdater by lazy {
        GitHubAutoUpdater(applicationContext)
    }

    private val viewModel: TikStudioViewModel by viewModels {
        TikStudioViewModel.Factory(
            extractorRepository = extractorRepository,
            downloadEngine = downloadEngine,
            downloadDao = database.downloadHistoryDao(),
            preferencesRepository = preferencesRepository,
            gitHubAutoUpdater = gitHubAutoUpdater
        )
    }

    private val overlayCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val state = ScreenRecorderForegroundService.recorderState.value
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            ScreenRecorderForegroundService.startRecordingSession(
                context = this,
                resultCode = result.resultCode,
                resultData = result.data,
                enableMic = state.isMicEnabled,
                qualityOption = state.selectedQuality
            )
            if (state.autoExitToBackgroundOnRecord) {
                ScreenRecorderForegroundService.exitAppToBackgroundForRecording(this)
            }
        } else {
            ScreenRecorderForegroundService.startRecordingSession(
                context = this,
                resultCode = Activity.RESULT_OK,
                resultData = null,
                enableMic = state.isMicEnabled,
                qualityOption = state.selectedQuality
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (viewModel.screenRecorderState.value.autoCloseOtherAppsOnOpen) {
            viewModel.closeOtherBackgroundAppsNow(this)
        }
        if (handleOverlayCaptureIntent(intent)) {
            // Handled screen capture request from floating overlay / notification
        } else if (!handleIncomingShareIntent(intent)) {
            checkClipboardForTikTokUrl()
        }

        setContent {
            TikStudioTheme {
                TikStudioMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkClipboardForTikTokUrl()
        viewModel.refreshServicesStatus(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Android 10+ grants clipboard access once the window gains focus
            checkClipboardForTikTokUrl()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!handleOverlayCaptureIntent(intent)) {
            handleIncomingShareIntent(intent)
        }
    }

    private fun handleOverlayCaptureIntent(intent: Intent?): Boolean {
        if (intent?.action == ScreenRecorderForegroundService.ACTION_REQUEST_CAPTURE_FROM_OVERLAY) {
            viewModel.selectMainTab(1)
            try {
                val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                val captureIntent = manager?.createScreenCaptureIntent()
                if (captureIntent != null) {
                    overlayCaptureLauncher.launch(captureIntent)
                } else {
                    ScreenRecorderForegroundService.startRecordingSession(this)
                }
            } catch (_: Exception) {
                ScreenRecorderForegroundService.startRecordingSession(this)
            }
            return true
        }
        return false
    }

    private fun checkClipboardForTikTokUrl() {
        val clipText = readClipboardText(this)
        if (clipText.isNotBlank()) {
            viewModel.autoDetectAndPasteFromClipboard(clipText)
        }
    }

    private fun handleIncomingShareIntent(intent: Intent?): Boolean {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val cleanUrl = extractorRepository.extractTikTokUrlFromClipboard(sharedText)
                    ?: extractorRepository.extractCleanUrl(sharedText)
                viewModel.onUrlInputChange(cleanUrl)
                viewModel.analyzeUrl(cleanUrl)
                return true
            }
        }
        return false
    }
}
