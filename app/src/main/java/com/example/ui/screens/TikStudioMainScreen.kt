package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
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
import com.example.R
import com.example.service.FloatingRadialRecorderWidget
import com.example.ui.components.DownloadedVaultItemCard
import com.example.ui.components.EmptyVaultPlaceholder
import com.example.ui.components.MediaInspectorCard
import com.example.ui.components.ScreenRecorderMiniPopupBar
import com.example.ui.components.ScreenRecorderUtilitySection
import com.example.ui.components.StudioMediaPreviewDialog
import com.example.ui.components.StudioSettingsBottomSheet
import com.example.ui.components.StudioTopHeader
import com.example.ui.components.SvgDrawableGraphic
import com.example.ui.components.TikTokShortcutFeatureSection
import com.example.ui.components.UrlCommandInputCard
import com.example.ui.components.VaultHeaderAndFilterRow
import com.example.ui.components.requestOverlayPermission
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.EmeraldBadgeBg
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioElevatedSurface
import com.example.ui.theme.StudioSurface
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
    val screenRecorderState by viewModel.screenRecorderState.collectAsStateWithLifecycle()
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
                    bottom = innerPadding.calculateBottomPadding() + 96.dp
                )
            ) {
                // 1. Minimalist Studio Header (Single Settings Button + V1.0.3 Badge)
                item(key = "header") {
                    StudioTopHeader(
                        hasUpdateAvailable = uiState.githubReleaseInfo?.isNewerThanCurrent == true,
                        onOpenSettings = { viewModel.openSettingsSheet(0) }
                    )
                }

                // 2. Top-Level SVG 2-Tab Switcher: "Tải TikTok Không Logo" vs "Các tiện ích"
                item(key = "main_svg_tab_bar") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(StudioSurface)
                            .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
                            .padding(4.dp)
                            .testTag("main_navigation_tabs"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Tab 0: Tải TikTok Không Logo
                        val isTab0 = uiState.selectedMainTab == 0
                        Surface(
                            onClick = { viewModel.selectMainTab(0) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isTab0) ElectricCyan.copy(alpha = 0.16f) else Color.Transparent,
                            border = if (isTab0) {
                                androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                            } else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("main_tab_0")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                SvgDrawableGraphic(
                                    drawableResId = R.drawable.ic_svg_tab_download,
                                    tint = if (isTab0) ElectricCyan else TextSecondary,
                                    size = 16.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tải Không Logo",
                                    fontSize = 12.sp,
                                    fontWeight = if (isTab0) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isTab0) ElectricCyan else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        // Tab 1: Các tiện ích (Separated from TikTok No-Logo Downloader)
                        val isTab1 = uiState.selectedMainTab == 1
                        Surface(
                            onClick = { viewModel.selectMainTab(1) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isTab1) Color(0xFFF97316).copy(alpha = 0.18f) else Color.Transparent,
                            border = if (isTab1) {
                                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316))
                            } else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("main_tab_1")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                SvgDrawableGraphic(
                                    drawableResId = R.drawable.ic_svg_tab_utilities,
                                    tint = if (isTab1) Color(0xFFF97316) else TextSecondary,
                                    size = 16.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Các tiện ích",
                                    fontSize = 12.sp,
                                    fontWeight = if (isTab1) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isTab1) Color(0xFFF97316) else TextSecondary,
                                    maxLines = 1
                                )
                                if (screenRecorderState.isRecording) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(NeonCoral)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Compact 1-Line Update Notification Pill
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
                                .clickable { viewModel.openSettingsSheet(2) }
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
                                SvgDrawableGraphic(
                                    drawableResId = R.drawable.ic_svg_addon_download,
                                    tint = EmeraldBadge,
                                    size = 16.dp
                                )
                                Text(
                                    text = "Có bản cập nhật ${releaseInfo.versionTag} • Nhấn để tải & cập nhật tự động",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Surface(
                                onClick = { viewModel.dismissUpdateBanner() },
                                shape = CircleShape,
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.selectedMainTab == 0) {
                    // =========================================================================
                    // TAB 0: TẢI TIKTOK KHÔNG LOGO (Strictly separated from Utilities)
                    // =========================================================================
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
                } else {
                    // =========================================================================
                    // TAB 1: CÁC TIỆN ÍCH (Dedicated Utilities Tab with Mandatory Download Gate)
                    // =========================================================================
                    item(key = "utilities_tab_intro") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioElevatedSurface)
                                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                SvgDrawableGraphic(
                                    drawableResId = R.drawable.ic_svg_tab_utilities,
                                    tint = Color(0xFFF97316),
                                    size = 18.dp
                                )
                                Column {
                                    Text(
                                        text = "Trung tâm Các Tiện Ích Mở Rộng (V1.0.4)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tải xuống từng tiện ích để kích hoạt Trình Ghi Màn Hình 4K & Phím tắt TikTok",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Utility 1: Trình Ghi Màn Hình (XRecorder-style Notification Bar + Radial Menu + 4K/Full HD)
                    item(key = "screen_recorder_utility") {
                        Spacer(modifier = Modifier.height(6.dp))
                        ScreenRecorderUtilitySection(
                            recorderState = screenRecorderState,
                            onInstallUtility = { viewModel.installScreenRecorderUtility(context) },
                            onSelectQuality = { viewModel.selectScreenRecordQuality(it) },
                            onOpenQualityDialog = { viewModel.openScreenRecordQualityDialog() },
                            onCloseQualityDialog = { viewModel.closeScreenRecordQualityDialog() },
                            onToggleFloatingBubble = { viewModel.toggleFloatingRecorderBubble(context, it) },
                            onToggleRadialMenu = { viewModel.toggleRecorderRadialMenu() },
                            onCaptureScreenshot = { viewModel.captureRecorderScreenshot(context) },
                            onToggleBrushTool = { viewModel.toggleRecorderBrushTool() },
                            onToggleMic = { viewModel.toggleScreenRecorderMic(it) },
                            onMicPermissionResult = { viewModel.setScreenRecorderMicPermission(it) },
                            onToggleAutoCloseOtherApps = { viewModel.toggleAutoCloseOtherAppsOnOpen(it) },
                            onCloseOtherAppsNow = { viewModel.closeOtherBackgroundAppsNow(context) },
                            onToggleAutoExitToBackground = { viewModel.toggleAutoExitToBackground(it) },
                            onStartScreenRecording = { resultCode, resultData, qualityOption ->
                                viewModel.startBackgroundScreenRecording(context, resultCode, resultData, qualityOption)
                            },
                            onPauseResumeScreenRecording = {
                                viewModel.togglePauseResumeScreenRecording(context)
                            },
                            onStopScreenRecording = {
                                viewModel.stopBackgroundScreenRecording(context)
                            },
                            onExitToBackgroundNow = {
                                viewModel.exitAppToRecordOtherApps(context)
                            }
                        )
                    }

                    // Utility 2: Tiện ích Phím tắt TikTok & Tự động lướt
                    item(key = "tiktok_shortcut_feature") {
                        Spacer(modifier = Modifier.height(8.dp))
                        TikTokShortcutFeatureSection(
                            settings = settings,
                            isUtilityDownloaded = uiState.isTikTokUtilityDownloaded,
                            isFloatingShortcutActive = uiState.isFloatingShortcutActive,
                            isAccessibilityActive = uiState.isAccessibilityServiceActive,
                            isAutoScrolling = uiState.isAutoScrolling,
                            autoScrollCountdown = uiState.autoScrollCountdown,
                            onDownloadUtility = { viewModel.downloadTikTokShortcutUtility() },
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

                    // Recorded High-Quality Videos & Screenshots Vault right inside Utilities Tab
                    val utilityCaptures = filteredDownloads.filter {
                        it.tikTokId.startsWith("screenrec_") || it.tikTokId.startsWith("screenshot_")
                    }.ifEmpty { filteredDownloads }

                    if (utilityCaptures.isNotEmpty()) {
                        item(key = "utility_recordings_header") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Video Quay Màn Hình & Ảnh Chụp Chất Lượng Cao (${utilityCaptures.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldBadge
                                )
                                Text(
                                    text = screenRecorderState.selectedQuality.shortLabel,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFFF97316)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(
                            items = utilityCaptures,
                            key = { "util_${it.id}" }
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

            // FLOATING EDGE CAMCORDER BUBBLE & RADIAL ARC FAN-OUT MENU (In-App Fallback when System Overlay is not yet attached)
            if (screenRecorderState.isUtilityInstalled &&
                !screenRecorderState.isSystemOverlayAttached &&
                (screenRecorderState.isFloatingBubbleEnabled || screenRecorderState.isRecording)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding() + 140.dp,
                            end = 4.dp
                        ),
                    contentAlignment = Alignment.TopEnd
                ) {
                    FloatingRadialRecorderWidget(
                        state = screenRecorderState,
                        onToggleExpand = { viewModel.toggleRecorderRadialMenu() },
                        onRecordOrStopClick = {
                            if (screenRecorderState.isRecording) {
                                viewModel.stopBackgroundScreenRecording(context)
                            } else {
                                viewModel.openScreenRecordQualityDialog()
                            }
                        },
                        onPauseResumeClick = {
                            viewModel.togglePauseResumeScreenRecording(context)
                        },
                        onHomeClick = {
                            viewModel.toggleRecorderRadialMenu()
                            viewModel.selectMainTab(1)
                        },
                        onScreenshotClick = {
                            viewModel.captureRecorderScreenshot(context)
                        },
                        onToolsClick = {
                            viewModel.openScreenRecordQualityDialog()
                        },
                        onBrushClick = {
                            viewModel.toggleRecorderBrushTool()
                        }
                    )
                }
            }

            // PERSISTENT MINI POP-UP CONTROL PANEL WHILE RECORDING
            if (screenRecorderState.isRecording) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding() + 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ScreenRecorderMiniPopupBar(
                        recorderState = screenRecorderState,
                        onPauseResume = { viewModel.togglePauseResumeScreenRecording(context) },
                        onToggleMic = {
                            viewModel.toggleScreenRecorderMic(!screenRecorderState.isMicEnabled)
                        },
                        onScreenshot = {
                            viewModel.captureRecorderScreenshot(context)
                        },
                        onSwitchToOtherApp = {
                            viewModel.exitAppToRecordOtherApps(context)
                        },
                        onStopAndSave = {
                            viewModel.stopBackgroundScreenRecording(context)
                        }
                    )
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
