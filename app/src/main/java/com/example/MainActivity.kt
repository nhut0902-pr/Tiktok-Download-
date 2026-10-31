package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.DownloadHistoryDatabase
import com.example.data.network.MediaDownloadEngine
import com.example.data.network.TikTokExtractorRepository
import com.example.data.preferences.StudioPreferencesRepository
import com.example.data.update.GitHubAutoUpdater
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!handleIncomingShareIntent(intent)) {
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
        handleIncomingShareIntent(intent)
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
