package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DownloadedVaultItemCard
import com.example.ui.components.EmptyVaultPlaceholder
import com.example.ui.components.MediaInspectorCard
import com.example.ui.components.StudioMediaPreviewDialog
import com.example.ui.components.StudioSettingsBottomSheet
import com.example.ui.components.StudioTopHeader
import com.example.ui.components.TikTokShortcutFeatureSection
import com.example.ui.components.UrlCommandInputCard
import com.example.ui.components.VaultHeaderAndFilterRow
import com.example.ui.components.requestOverlayPermission
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.EmeraldBadgeBg
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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
    val storageSummary by viewModel.vaultStorageSummary.collectAsStateWithLifecycle()

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
                            ElectricCyan.copy(alpha = 0.06f),
                            NeonCoral.copy(alpha = 0.03f),
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
                // 1. Minimalist Studio Header (Single Settings Button)
                item(key = "header") {
                    StudioTopHeader(
                        hasUpdateAvailable = uiState.githubReleaseInfo?.isNewerThanCurrent == true,
                        onOpenSettings = { viewModel.openSettingsSheet(0) }
                    )
                }

                // 2. Compact 1-Line Update Notification Pill (Only shown when version 1.0.2 is not yet applied)
                val releaseInfo = uiState.githubReleaseInfo
                if (releaseInfo != null && releaseInfo.isNewerThanCurrent && !uiState.isUpdateBannerDismissed) {
                    item(key = "github_update_banner") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldBadgeBg)
                                .border(1.dp, EmeraldBadge.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .clickable { viewModel.openSettingsSheet(1) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("github_update_banner"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldBadge.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SystemUpdate,
                                        contentDescription = null,
                                        tint = EmeraldBadge,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = "Có bản cập nhật ${releaseInfo.versionTag} từ GitHub • Nhấn để tải tự động",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.dismissUpdateBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Đóng",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // 3. URL & Clipboard Command Bar + Instant Coil Thumbnail Preview + Batch Queue Downloader
                item(key = "url_command_bar") {
                    Spacer(modifier = Modifier.height(6.dp))
                    UrlCommandInputCard(
                        urlInput = uiState.urlInput,
                        isUrlValid = uiState.isUrlValid,
                        validatedThumbnailPreview = uiState.validatedThumbnailPreview,
                        isBatchModeExpanded = uiState.isBatchModeExpanded,
                        batchInputText = uiState.batchInputText,
                        batchQueueProgress = uiState.batchQueueProgress,
                        isInspecting = uiState.isInspecting,
                        statusMessage = uiState.statusMessage,
                        isStatusError = uiState.isStatusError,
                        onUrlChange = { viewModel.onUrlInputChange(it) },
                        onClearUrl = { viewModel.onUrlInputChange("") },
                        onPasteClick = {
                            val clipText = readClipboardText(context)
                            viewModel.onPasteAndAnalyze(clipText)
                        },
                        onAnalyzeClick = { viewModel.analyzeUrl() },
                        onThumbnailPreviewClick = { viewModel.openLivePreview(0) },
                        onDownloadCoverClick = { viewModel.downloadCoverThumbnailHd() },
                        onToggleBatchMode = { viewModel.toggleBatchMode() },
                        onBatchInputChange = { viewModel.onBatchInputChange(it) },
                        onStartBatchDownload = { viewModel.startBatchDownload() }
                    )
                }

                // 3.5. TikTok Shortcut in-App & Hands-Free Auto-Scroll Feature Card
                item(key = "tiktok_shortcut_feature") {
                    Spacer(modifier = Modifier.height(10.dp))
                    TikTokShortcutFeatureSection(
                        settings = settings,
                        isFloatingShortcutActive = uiState.isFloatingShortcutActive,
                        isAccessibilityActive = uiState.isAccessibilityServiceActive,
                        isAutoScrolling = uiState.isAutoScrolling,
                        autoScrollCountdown = uiState.autoScrollCountdown,
                        onToggleFloatingShortcut = {
                            val success = viewModel.toggleFloatingShortcut(context)
                            if (!success) {
                                requestOverlayPermission(context)
                            }
                        },
                        onToggleAutoScroll = {
                            viewModel.toggleAutoScroll()
                        },
                        onTestSwipeNext = {
                            viewModel.triggerNextVideoSwipe()
                        },
                        onLaunchTikTok = {
                            viewModel.launchTikTokApp(context)
                        },
                        onOpenSettingsTab = {
                            viewModel.openSettingsSheet(1)
                        }
                    )
                }

                // 4. Dynamic Media Inspector Card + Creator Toolkit (Cover HD, MP3 320k, Hashtags, Viral Script)
                uiState.inspectedMedia?.let { media ->
                    item(key = "media_inspector_${media.id}") {
                        Spacer(modifier = Modifier.height(14.dp))
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
                                viewModel.notifyCopiedToClipboard("đường dẫn luồng gốc")
                            },
                            onCopyCaption = { caption ->
                                copyTextToClipboard(context, caption)
                                viewModel.notifyCopiedToClipboard("Caption gốc")
                            },
                            onCopyHashtags = { hashtags ->
                                copyTextToClipboard(context, hashtags)
                                viewModel.notifyCopiedToClipboard("bộ Hashtag")
                            },
                            onCopyViralScript = { script ->
                                copyTextToClipboard(context, script)
                                viewModel.notifyCopiedToClipboard("mẫu Caption & Hashtag Reup")
                            },
                            onDownloadCoverHd = { viewModel.downloadCoverThumbnailHd() },
                            onDownloadQuickMp3 = { viewModel.downloadQuickAudioMp3() },
                            onOpenLivePreview = { photoIdx ->
                                viewModel.openLivePreview(photoIdx)
                            },
                            onStartDownload = { viewModel.startPrimaryDownload() }
                        )
                    }
                }

                // 5. Recent Downloads Vault Header, Storage Analytics, Search & Filter Chips
                item(key = "vault_header") {
                    Spacer(modifier = Modifier.height(20.dp))
                    VaultHeaderAndFilterRow(
                        totalCount = totalCount,
                        storageSummary = storageSummary,
                        selectedFilter = uiState.selectedVaultFilter,
                        searchQuery = uiState.vaultSearchQuery,
                        onSelectFilter = { viewModel.selectVaultFilter(it) },
                        onSearchQueryChange = { viewModel.onVaultSearchQueryChange(it) },
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
                            onToggleFavoriteClick = { viewModel.toggleFavoriteDownload(it) },
                            onReInspectClick = { viewModel.reInspectSavedItem(it) },
                            onShareClick = { viewModel.shareDownloadedFile(context, it) },
                            onDeleteClick = { viewModel.deleteDownloadItem(it) }
                        )
                    }
                }
            }
        }
    }

    // Unified Settings & App Update Bottom Sheet
    if (uiState.isSettingsSheetOpen) {
        StudioSettingsBottomSheet(
            settings = settings,
            currentVersionName = uiState.currentVersionName,
            releaseInfo = uiState.githubReleaseInfo,
            isCheckingUpdate = uiState.isCheckingGitHubUpdate,
            apkDownloadProgress = uiState.apkUpdateDownloadProgress,
            initialTab = uiState.settingsInitialTab,
            onDismiss = { viewModel.closeSettingsSheet() },
            onSelectDefaultQuality = { viewModel.setDefaultQuality(it) },
            onToggleForceNoWatermark = { viewModel.setForceNoWatermark(it) },
            onToggleAutoDetectClipboard = { viewModel.setAutoDetectClipboard(it) },
            onToggleSaveToGallery = { viewModel.setSaveToPublicGallery(it) },
            onToggleIncludeMp3WithCarousel = { viewModel.setIncludeMp3WithCarousel(it) },
            onSelectNamingRule = { viewModel.setFileNamingRule(it) },
            onUpdateRepoSlug = { viewModel.setGitHubRepoSlug(it) },
            onToggleAutoCheckUpdate = { viewModel.setAutoCheckGitHubUpdate(it) },
            onCheckLatestRelease = { viewModel.checkForGitHubUpdates(repoOverride = it) },
            onStartAutoUpdate = { viewModel.startAutoUpdateAndInstallApk() },
            onInstallReadyApk = { viewModel.installReadyApk() },
            onCopyWorkflowYaml = { yaml ->
                copyTextToClipboard(context, yaml)
            },
            onToggleFloatingShortcut = {
                val success = viewModel.toggleFloatingShortcut(context)
                if (!success) {
                    requestOverlayPermission(context)
                }
            },
            onToggleAutoSwipeAfterDownload = { viewModel.updateAutoSwipeAfterDownload(it) },
            onSelectSwipeDelaySeconds = { viewModel.updateSwipeDelaySeconds(it) },
            onSelectAutoScrollInterval = { viewModel.updateAutoScrollInterval(it) },
            onToggleAutoDownloadOnScroll = { viewModel.updateAutoDownloadOnScroll(it) }
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
