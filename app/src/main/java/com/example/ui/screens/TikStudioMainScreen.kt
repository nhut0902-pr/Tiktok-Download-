package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.ui.components.DownloadedVaultItemCard
import com.example.ui.components.EmptyVaultPlaceholder
import com.example.ui.components.GitHubUpdateNotificationBanner
import com.example.ui.components.GitHubWorkflowAndUpdateDialog
import com.example.ui.components.MediaInspectorCard
import com.example.ui.components.StudioMediaPreviewDialog
import com.example.ui.components.StudioSettingsBottomSheet
import com.example.ui.components.StudioTopHeader
import com.example.ui.components.UrlCommandInputCard
import com.example.ui.components.VaultHeaderAndFilterRow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.StudioBackground
import com.example.ui.viewmodel.TikStudioViewModel

@Composable
fun TikStudioMainScreen(
    viewModel: TikStudioViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val windowInfo = LocalWindowInfo.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val filteredDownloads by viewModel.filteredDownloads.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalDownloadsCount.collectAsStateWithLifecycle()

    // Automatically detect and paste TikTok URL from clipboard when screen opens or resumes
    DisposableEffect(lifecycleOwner, settings.autoDetectClipboard) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                val clipText = readClipboardText(context)
                if (clipText.isNotBlank()) {
                    viewModel.autoDetectAndPasteFromClipboard(clipText)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // On Android 10+, clipboard access becomes available as soon as the window gains focus
    LaunchedEffect(windowInfo.isWindowFocused, settings.autoDetectClipboard) {
        if (windowInfo.isWindowFocused && settings.autoDetectClipboard) {
            val clipText = readClipboardText(context)
            if (clipText.isNotBlank()) {
                viewModel.autoDetectAndPasteFromClipboard(clipText)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = StudioBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBackground)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.08f),
                            NeonCoral.copy(alpha = 0.04f),
                            StudioBackground
                        ),
                        center = Offset(300f, 180f),
                        radius = 900f
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 28.dp
                )
            ) {
                // 1. Top Studio Header
                item(key = "header") {
                    StudioTopHeader(
                        defaultQuality = settings.defaultQualityTier,
                        hasUpdateAvailable = uiState.githubReleaseInfo?.isNewerThanCurrent == true,
                        onOpenGitHubUpdater = { viewModel.openGitHubWorkflowDialog() },
                        onOpenSettings = { viewModel.openSettingsSheet() }
                    )
                }

                // 2. URL & Clipboard Command Bar + Instant Coil Thumbnail Preview + Sample Showcase Chips
                item(key = "url_command_bar") {
                    Spacer(modifier = Modifier.height(4.dp))
                    UrlCommandInputCard(
                        urlInput = uiState.urlInput,
                        isUrlValid = uiState.isUrlValid,
                        validatedThumbnailPreview = uiState.validatedThumbnailPreview,
                        isInspecting = uiState.isInspecting,
                        sampleLinks = uiState.sampleLinks,
                        statusMessage = uiState.statusMessage,
                        isStatusError = uiState.isStatusError,
                        onUrlChange = { viewModel.onUrlInputChange(it) },
                        onClearUrl = { viewModel.onUrlInputChange("") },
                        onPasteClick = {
                            val clipText = readClipboardText(context)
                            viewModel.onPasteAndAnalyze(clipText)
                        },
                        onAnalyzeClick = { viewModel.analyzeUrl() },
                        onSelectSample = { viewModel.onSelectSampleLink(it) },
                        onThumbnailPreviewClick = { viewModel.openLivePreview(0) }
                    )
                }

                // 3. GitHub Auto-Update Notification Banner (shown when new GitHub Release APK is ready)
                val releaseInfo = uiState.githubReleaseInfo
                if (releaseInfo != null && releaseInfo.isNewerThanCurrent && !uiState.isUpdateBannerDismissed) {
                    item(key = "github_update_banner") {
                        Spacer(modifier = Modifier.height(10.dp))
                        GitHubUpdateNotificationBanner(
                            release = releaseInfo,
                            currentVersionName = BuildConfig.VERSION_NAME,
                            apkDownloadProgress = uiState.apkUpdateDownloadProgress,
                            onStartAutoUpdate = { viewModel.startAutoUpdateAndInstallApk() },
                            onInstallReadyApk = { viewModel.installReadyApk() },
                            onOpenWorkflowDialog = { viewModel.openGitHubWorkflowDialog() },
                            onDismissBanner = { viewModel.dismissUpdateBanner() }
                        )
                    }
                }

                // 4. Dynamic Media Inspector Card (Video 1080p/720p/MP3 or Photo Carousel)
                uiState.inspectedMedia?.let { media ->
                    item(key = "media_inspector_${media.id}") {
                        Spacer(modifier = Modifier.height(16.dp))
                        MediaInspectorCard(
                            media = media,
                            customOptions = uiState.customOptions,
                            forceNoWatermark = settings.forceNoWatermark,
                            isCustomOptionsExpanded = uiState.isCustomOptionsExpanded,
                            activeDownload = uiState.activeDownload,
                            onSelectTier = { viewModel.selectQualityTier(it) },
                            onTogglePhotoIndex = { viewModel.togglePhotoSelection(it) },
                            onSelectAllPhotos = { viewModel.selectAllPhotos() },
                            onDeselectAllPhotos = { viewModel.deselectAllPhotos() },
                            onToggleIncludeAudioWithPhotos = { viewModel.toggleIncludeAudioWithPhotos() },
                            onToggleCustomOptionsExpanded = { viewModel.toggleCustomOptionsExpanded() },
                            onUpdateCustomPrefix = { viewModel.updateCustomFilePrefix(it) },
                            onCopyDirectUrl = { url ->
                                copyTextToClipboard(context, url)
                            },
                            onOpenLivePreview = { photoIdx ->
                                viewModel.openLivePreview(photoIdx)
                            },
                            onStartDownload = { viewModel.startPrimaryDownload() }
                        )
                    }
                }

                // 5. Recent Downloads Vault Header & Filter Chips
                item(key = "vault_header") {
                    Spacer(modifier = Modifier.height(22.dp))
                    VaultHeaderAndFilterRow(
                        totalCount = totalCount,
                        selectedFilter = uiState.selectedVaultFilter,
                        onSelectFilter = { viewModel.selectVaultFilter(it) },
                        onClearAll = { viewModel.clearAllDownloads() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 6. Downloaded Items List or Empty Vault State
                if (filteredDownloads.isEmpty()) {
                    item(key = "empty_vault") {
                        EmptyVaultPlaceholder()
                    }
                } else {
                    items(
                        items = filteredDownloads,
                        key = { it.id }
                    ) { item ->
                        DownloadedVaultItemCard(
                            item = item,
                            onPreviewClick = { viewModel.openSavedDownloadPreview(it) },
                            onShareClick = { viewModel.shareDownloadedFile(context, it) },
                            onDeleteClick = { viewModel.deleteDownloadItem(it) }
                        )
                    }
                }
            }
        }
    }

    // Settings Bottom Sheet
    if (uiState.isSettingsSheetOpen) {
        StudioSettingsBottomSheet(
            settings = settings,
            onDismiss = { viewModel.closeSettingsSheet() },
            onSelectDefaultQuality = { viewModel.setDefaultQuality(it) },
            onToggleForceNoWatermark = { viewModel.setForceNoWatermark(it) },
            onToggleAutoDetectClipboard = { viewModel.setAutoDetectClipboard(it) },
            onToggleSaveToGallery = { viewModel.setSaveToPublicGallery(it) },
            onToggleIncludeMp3WithCarousel = { viewModel.setIncludeMp3WithCarousel(it) },
            onSelectNamingRule = { viewModel.setFileNamingRule(it) }
        )
    }

    // GitHub CI/CD Workflow & Auto-Update Modal
    if (uiState.isGitHubWorkflowDialogOpen) {
        GitHubWorkflowAndUpdateDialog(
            githubRepoSlug = settings.githubRepoSlug,
            autoCheckEnabled = settings.autoCheckGitHubUpdate,
            currentVersionName = BuildConfig.VERSION_NAME,
            releaseInfo = uiState.githubReleaseInfo,
            isCheckingUpdate = uiState.isCheckingGitHubUpdate,
            apkDownloadProgress = uiState.apkUpdateDownloadProgress,
            onUpdateRepoSlug = { viewModel.setGitHubRepoSlug(it) },
            onToggleAutoCheck = { viewModel.setAutoCheckGitHubUpdate(it) },
            onCheckLatestRelease = { viewModel.checkForGitHubUpdates(repoOverride = it) },
            onStartAutoUpdate = { viewModel.startAutoUpdateAndInstallApk() },
            onInstallReadyApk = { viewModel.installReadyApk() },
            onCopyWorkflowYaml = { yaml ->
                copyTextToClipboard(context, yaml)
            },
            onDismiss = { viewModel.closeGitHubWorkflowDialog() }
        )
    }

    // Live Video / Photo Carousel Preview Modal
    uiState.previewTarget?.let { target ->
        StudioMediaPreviewDialog(
            target = target,
            onDismiss = { viewModel.closePreviewModal() }
        )
    }
}

fun readClipboardText(context: Context): String {
    return try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            (item.text ?: item.coerceToText(context))?.toString().orEmpty()
        } else {
            ""
        }
    } catch (_: Exception) {
        ""
    }
}

private fun copyTextToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("TikStudio Direct Link", text)
        clipboard?.setPrimaryClip(clip)
    } catch (_: Exception) {
        // Ignore
    }
}
