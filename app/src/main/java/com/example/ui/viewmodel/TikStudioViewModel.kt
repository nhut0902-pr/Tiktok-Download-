package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.DownloadHistoryDao
import com.example.data.local.DownloadItemEntity
import com.example.data.model.ActiveDownloadProgress
import com.example.data.model.BatchQueueProgress
import com.example.data.model.CustomDownloadOptions
import com.example.data.model.SampleTikTokLink
import com.example.data.model.TikTokMediaInfo
import com.example.data.model.ValidatedThumbnailPreview
import com.example.data.model.VaultStorageSummary
import com.example.data.model.VideoQualityTier
import com.example.data.network.MediaDownloadEngine
import com.example.data.network.TikTokExtractorRepository
import com.example.data.preferences.FileNamingRule
import com.example.data.preferences.StudioPreferencesRepository
import com.example.data.preferences.StudioSettings
import com.example.data.update.ApkUpdateDownloadProgress
import com.example.data.update.GitHubAutoUpdater
import com.example.data.update.GitHubReleaseInfo
import com.example.service.FloatingTikTokShortcutService
import com.example.service.TikAutoScrollAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed class PreviewModalTarget {
    data class LiveMedia(val media: TikTokMediaInfo, val initialPhotoIndex: Int = 0) : PreviewModalTarget()
    data class SavedDownload(val item: DownloadItemEntity) : PreviewModalTarget()
}

data class StudioUiState(
    val urlInput: String = "",
    val isUrlValid: Boolean = false,
    val validatedThumbnailPreview: ValidatedThumbnailPreview? = null,
    val isBatchModeExpanded: Boolean = false,
    val batchInputText: String = "",
    val batchQueueProgress: BatchQueueProgress? = null,
    val isInspecting: Boolean = false,
    val inspectedMedia: TikTokMediaInfo? = null,
    val customOptions: CustomDownloadOptions = CustomDownloadOptions(),
    val isCustomOptionsExpanded: Boolean = false,
    val activeDownload: ActiveDownloadProgress? = null,
    val statusMessage: String? = null,
    val isStatusError: Boolean = false,
    val selectedVaultFilter: String = "ALL", // "ALL", "FAVORITE", "VIDEO", "PHOTO", "AUDIO"
    val vaultSearchQuery: String = "",
    val isSettingsSheetOpen: Boolean = false,
    val settingsInitialTab: Int = 0,
    val previewTarget: PreviewModalTarget? = null,
    val sampleLinks: List<SampleTikTokLink> = emptyList(),
    val githubReleaseInfo: GitHubReleaseInfo? = null,
    val currentVersionName: String = "1.0.1",
    val isCheckingGitHubUpdate: Boolean = false,
    val apkUpdateDownloadProgress: ApkUpdateDownloadProgress? = null,
    val isUpdateBannerDismissed: Boolean = false,
    val isGitHubWorkflowDialogOpen: Boolean = false,
    val isFloatingShortcutActive: Boolean = false,
    val isAccessibilityServiceActive: Boolean = false,
    val isAutoScrolling: Boolean = false,
    val autoScrollCountdown: Int = 10
)

class TikStudioViewModel(
    private val extractorRepository: TikTokExtractorRepository,
    private val downloadEngine: MediaDownloadEngine,
    private val downloadDao: DownloadHistoryDao,
    private val preferencesRepository: StudioPreferencesRepository,
    private val gitHubAutoUpdater: GitHubAutoUpdater
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        StudioUiState(
            sampleLinks = extractorRepository.sampleLinks,
            currentVersionName = preferencesRepository.getEffectiveVersionName(BuildConfig.VERSION_NAME)
        )
    )
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val settings: StateFlow<StudioSettings> = preferencesRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StudioSettings()
        )

    private val allDownloads: StateFlow<List<DownloadItemEntity>> = downloadDao.getAllDownloads()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredDownloads: StateFlow<List<DownloadItemEntity>> = combine(
        allDownloads,
        _uiState
    ) { items, state ->
        val byCategory = when (state.selectedVaultFilter) {
            "FAVORITE" -> items.filter { it.isFavorite }
            "VIDEO" -> items.filter { it.mediaType == "VIDEO" }
            "PHOTO" -> items.filter { it.mediaType == "PHOTO" }
            "AUDIO" -> items.filter { it.mediaType == "AUDIO" }
            else -> items
        }
        val query = state.vaultSearchQuery.trim().lowercase()
        if (query.isBlank()) {
            byCategory
        } else {
            byCategory.filter { item ->
                item.title.lowercase().contains(query) ||
                    item.authorName.lowercase().contains(query) ||
                    item.authorHandle.lowercase().contains(query) ||
                    item.qualityLabel.lowercase().contains(query)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalDownloadsCount: StateFlow<Int> = combine(allDownloads) { items ->
        items[0].size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val vaultStorageSummary: StateFlow<VaultStorageSummary> = combine(allDownloads) { arr ->
        val list = arr[0]
        VaultStorageSummary(
            totalFiles = list.size,
            totalBytes = list.sumOf { it.fileSizeBytes },
            videoCount = list.count { it.mediaType == "VIDEO" },
            photoCount = list.count { it.mediaType == "PHOTO" },
            audioCount = list.count { it.mediaType == "AUDIO" },
            favoriteCount = list.count { it.isFavorite }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultStorageSummary()
    )

    private var lastAutoDetectedClipboardUrl: String? = null
    private var lastAutoInspectedValidatedUrl: String? = null

    init {
        // Automatically check GitHub Releases for a newer APK build on app open
        checkForGitHubUpdates(isStartupCheck = true)

        viewModelScope.launch {
            TikAutoScrollAccessibilityService.autoScrollState.collect { autoState ->
                _uiState.update {
                    it.copy(
                        isAutoScrolling = autoState.isAutoScrolling,
                        autoScrollCountdown = autoState.countdownRemaining,
                        isAccessibilityServiceActive = TikAutoScrollAccessibilityService.isServiceActive
                    )
                }
            }
        }
    }

    fun onUrlInputChange(newValue: String) {
        val immediatePreview = extractorRepository.getImmediateThumbnailPreview(newValue)
        _uiState.update {
            it.copy(
                urlInput = newValue,
                isUrlValid = immediatePreview != null,
                validatedThumbnailPreview = immediatePreview,
                statusMessage = if (immediatePreview != null) {
                    "URL TikTok hợp lệ • Đang hiển thị ảnh bìa xem trước"
                } else {
                    null
                },
                isStatusError = false
            )
        }

        if (immediatePreview != null && immediatePreview.validatedUrl != lastAutoInspectedValidatedUrl) {
            lastAutoInspectedValidatedUrl = immediatePreview.validatedUrl
            enrichThumbnailAndInspect(immediatePreview.validatedUrl, updateUrlField = false)
        } else if (immediatePreview == null) {
            lastAutoInspectedValidatedUrl = null
        }
    }

    private fun enrichThumbnailAndInspect(
        validatedUrl: String,
        updateUrlField: Boolean,
        completionBannerOverride: String? = null
    ) {
        viewModelScope.launch {
            val oembedPreview = extractorRepository.fetchLiveOEmbedThumbnail(validatedUrl)
            if (oembedPreview != null && _uiState.value.isUrlValid) {
                _uiState.update { state ->
                    state.copy(validatedThumbnailPreview = oembedPreview)
                }
            }
        }
        analyzeUrl(
            rawUrl = validatedUrl,
            silentStatus = completionBannerOverride == null,
            updateUrlField = updateUrlField,
            completionBannerOverride = completionBannerOverride
        )
    }

    /**
     * Automatically detects a TikTok/Douyin URL from the device clipboard when the application
     * is opened or resumed, pastes it into the input field, and triggers stream inspection.
     */
    fun autoDetectAndPasteFromClipboard(rawClipboardText: String?): Boolean {
        if (!settings.value.autoDetectClipboard) return false
        val detectedUrl = extractorRepository.extractTikTokUrlFromClipboard(rawClipboardText)
            ?: return false

        // Avoid re-triggering if the same clipboard URL was already auto-pasted and is still in the input field
        if (detectedUrl == lastAutoDetectedClipboardUrl && _uiState.value.urlInput == detectedUrl) {
            return false
        }

        lastAutoDetectedClipboardUrl = detectedUrl
        lastAutoInspectedValidatedUrl = detectedUrl
        val immediatePreview = extractorRepository.getImmediateThumbnailPreview(detectedUrl)
        val autoPasteBanner = "Đã tự động phát hiện & dán link TikTok từ bộ nhớ tạm!"
        _uiState.update {
            it.copy(
                urlInput = detectedUrl,
                isUrlValid = immediatePreview != null,
                validatedThumbnailPreview = immediatePreview,
                statusMessage = autoPasteBanner,
                isStatusError = false
            )
        }
        enrichThumbnailAndInspect(
            validatedUrl = detectedUrl,
            updateUrlField = true,
            completionBannerOverride = autoPasteBanner
        )
        return true
    }

    fun onPasteAndAnalyze(clipboardText: String) {
        if (clipboardText.isBlank()) {
            _uiState.update {
                it.copy(
                    statusMessage = "Khay nhớ tạm (Clipboard) đang trống. Hãy sao chép link từ TikTok hoặc chọn link mẫu bên dưới.",
                    isStatusError = true
                )
            }
            return
        }
        val clean = extractorRepository.extractTikTokUrlFromClipboard(clipboardText)
            ?: extractorRepository.extractCleanUrl(clipboardText)
        val immediatePreview = extractorRepository.getImmediateThumbnailPreview(clean)
        lastAutoDetectedClipboardUrl = clean
        lastAutoInspectedValidatedUrl = immediatePreview?.validatedUrl
        _uiState.update {
            it.copy(
                urlInput = clean,
                isUrlValid = immediatePreview != null,
                validatedThumbnailPreview = immediatePreview
            )
        }
        analyzeUrl(clean)
    }

    fun onSelectSampleLink(sample: SampleTikTokLink) {
        val immediatePreview = extractorRepository.getImmediateThumbnailPreview(sample.url)
        lastAutoInspectedValidatedUrl = sample.url
        _uiState.update {
            it.copy(
                urlInput = sample.url,
                isUrlValid = immediatePreview != null,
                validatedThumbnailPreview = immediatePreview,
                statusMessage = null
            )
        }
        analyzeUrl(sample.url)
    }

    fun analyzeUrl(
        rawUrl: String = _uiState.value.urlInput,
        silentStatus: Boolean = false,
        updateUrlField: Boolean = true,
        completionBannerOverride: String? = null
    ) {
        if (rawUrl.isBlank()) {
            _uiState.update {
                it.copy(
                    statusMessage = "Vui lòng nhập hoặc dán liên kết TikTok trước khi phân tích.",
                    isStatusError = true
                )
            }
            return
        }

        val cleanUrl = extractorRepository.extractCleanUrl(rawUrl)
        val immediatePreview = extractorRepository.getImmediateThumbnailPreview(cleanUrl)
        if (updateUrlField) {
            _uiState.update {
                it.copy(
                    urlInput = cleanUrl,
                    isUrlValid = immediatePreview != null,
                    validatedThumbnailPreview = immediatePreview ?: it.validatedThumbnailPreview
                )
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInspecting = true,
                    statusMessage = completionBannerOverride
                        ?: if (silentStatus) it.statusMessage else "Đang bóc tách luồng No-Watermark HD / Full HD...",
                    isStatusError = false
                )
            }

            val currentSettings = settings.value
            val result = extractorRepository.inspectTikTokUrl(cleanUrl)

            result.fold(
                onSuccess = { media ->
                    val preferredTier = if (media.isPhotoSlideshow) {
                        VideoQualityTier.AUDIO_MP3
                    } else {
                        val defaultTier = currentSettings.defaultQualityTier
                        if (media.streams.any { it.tier == defaultTier }) {
                            defaultTier
                        } else {
                            media.streams.firstOrNull()?.tier ?: VideoQualityTier.FULL_HD_1080P
                        }
                    }

                    val allPhotoIndices = media.photos.map { it.index }.toSet()
                    val resolvedThumb = ValidatedThumbnailPreview(
                        validatedUrl = cleanUrl,
                        videoId = media.id,
                        coverImageUrl = media.coverUrl,
                        title = media.title,
                        authorName = media.authorName,
                        authorHandle = media.authorHandle,
                        durationSeconds = media.durationSeconds,
                        isPhotoSlideshow = media.isPhotoSlideshow,
                        photoCount = media.photos.size,
                        resolutionBadge = if (media.isPhotoSlideshow) {
                            "BỘ ${media.photos.size} ẢNH HD • NO LOGO"
                        } else {
                            "FULL HD 1080P • NO LOGO"
                        }
                    )

                    _uiState.update {
                        it.copy(
                            isInspecting = false,
                            inspectedMedia = media,
                            validatedThumbnailPreview = if (it.isUrlValid || updateUrlField) {
                                resolvedThumb
                            } else {
                                it.validatedThumbnailPreview
                            },
                            customOptions = CustomDownloadOptions(
                                selectedTier = preferredTier,
                                customFilePrefix = "",
                                includeAudioWithPhotos = currentSettings.includeMp3WithCarousel,
                                force60FpsMetadata = true,
                                selectedPhotoIndices = allPhotoIndices
                            ),
                            statusMessage = completionBannerOverride ?: if (silentStatus) {
                                it.statusMessage
                            } else if (media.isPhotoSlideshow) {
                                "Đã tìm thấy bộ ${media.photos.size} ảnh HD Không Logo + Nhạc nền gốc!"
                            } else {
                                "Đã sẵn sàng tải Không Logo • Chất lượng lên tới Full HD 1080p!"
                            },
                            isStatusError = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isInspecting = false,
                            statusMessage = error.message ?: "Không thể phân tích liên kết TikTok.",
                            isStatusError = true
                        )
                    }
                }
            )
        }
    }

    fun selectQualityTier(tier: VideoQualityTier) {
        _uiState.update { state ->
            state.copy(
                customOptions = state.customOptions.copy(selectedTier = tier)
            )
        }
    }

    fun toggleCustomOptionsExpanded() {
        _uiState.update { it.copy(isCustomOptionsExpanded = !it.isCustomOptionsExpanded) }
    }

    fun updateCustomFilePrefix(prefix: String) {
        _uiState.update { state ->
            state.copy(
                customOptions = state.customOptions.copy(customFilePrefix = prefix)
            )
        }
    }

    fun toggleIncludeAudioWithPhotos() {
        _uiState.update { state ->
            state.copy(
                customOptions = state.customOptions.copy(
                    includeAudioWithPhotos = !state.customOptions.includeAudioWithPhotos
                )
            )
        }
    }

    fun togglePhotoSelection(photoIndex: Int) {
        _uiState.update { state ->
            val current = state.customOptions.selectedPhotoIndices.toMutableSet()
            if (current.contains(photoIndex)) {
                current.remove(photoIndex)
            } else {
                current.add(photoIndex)
            }
            state.copy(
                customOptions = state.customOptions.copy(selectedPhotoIndices = current)
            )
        }
    }

    fun selectAllPhotos() {
        val media = _uiState.value.inspectedMedia ?: return
        val allIndices = media.photos.map { it.index }.toSet()
        _uiState.update { state ->
            state.copy(
                customOptions = state.customOptions.copy(selectedPhotoIndices = allIndices)
            )
        }
    }

    fun deselectAllPhotos() {
        _uiState.update { state ->
            state.copy(
                customOptions = state.customOptions.copy(selectedPhotoIndices = emptySet())
            )
        }
    }

    fun startPrimaryDownload() {
        val state = _uiState.value
        val media = state.inspectedMedia ?: return
        if (state.activeDownload != null) return

        viewModelScope.launch {
            val currentSettings = settings.value
            val customPrefix = state.customOptions.customFilePrefix

            if (media.isPhotoSlideshow) {
                val chosenPhotos = media.photos.filter {
                    state.customOptions.selectedPhotoIndices.contains(it.index)
                }
                if (chosenPhotos.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            statusMessage = "Hãy chọn ít nhất 1 ảnh trong bộ slideshow để tải về.",
                            isStatusError = true
                        )
                    }
                    return@launch
                }

                val res = downloadEngine.downloadPhotoCarousel(
                    media = media,
                    selectedPhotos = chosenPhotos,
                    includeAudioMp3 = state.customOptions.includeAudioWithPhotos,
                    settings = currentSettings,
                    customPrefix = customPrefix,
                    onProgress = { progress ->
                        _uiState.update { it.copy(activeDownload = progress) }
                    }
                )

                res.fold(
                    onSuccess = { savedList ->
                        _uiState.update {
                            it.copy(
                                activeDownload = null,
                                statusMessage = "Đã tải thành công ${savedList.size} tệp Không Logo vào Kho lưu trữ & Thư viện!",
                                isStatusError = false
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                activeDownload = null,
                                statusMessage = "Lỗi khi tải bộ ảnh: ${err.message}",
                                isStatusError = true
                            )
                        }
                    }
                )
            } else {
                val chosenTier = state.customOptions.selectedTier
                val stream = media.streams.find { it.tier == chosenTier }
                    ?: media.streams.firstOrNull()
                    ?: return@launch

                val res = downloadEngine.downloadVideoOrAudio(
                    media = media,
                    stream = stream,
                    settings = currentSettings,
                    customPrefix = customPrefix,
                    onProgress = { progress ->
                        _uiState.update { it.copy(activeDownload = progress) }
                    }
                )

                res.fold(
                    onSuccess = { savedItem ->
                        _uiState.update {
                            it.copy(
                                activeDownload = null,
                                statusMessage = "Hoàn tất tải ${savedItem.qualityLabel} Không Logo (${savedItem.resolution})!",
                                isStatusError = false
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                activeDownload = null,
                                statusMessage = "Lỗi tải xuống: ${err.message}",
                                isStatusError = true
                            )
                        }
                    }
                )
            }
        }
    }

    fun selectVaultFilter(filter: String) {
        _uiState.update { it.copy(selectedVaultFilter = filter) }
    }

    fun onVaultSearchQueryChange(query: String) {
        _uiState.update { it.copy(vaultSearchQuery = query) }
    }

    fun toggleFavoriteDownload(item: DownloadItemEntity) {
        viewModelScope.launch {
            val nextFav = !item.isFavorite
            downloadDao.setFavorite(item.id, nextFav)
            _uiState.update {
                it.copy(
                    statusMessage = if (nextFav) {
                        "Đã thêm vào danh sách Yêu thích ★"
                    } else {
                        "Đã bỏ đánh dấu Yêu thích."
                    },
                    isStatusError = false
                )
            }
        }
    }

    fun reInspectSavedItem(item: DownloadItemEntity) {
        val url = item.sourceTikTokUrl.ifBlank { return }
        onUrlInputChange(url)
        analyzeUrl(url)
    }

    fun downloadCoverThumbnailHd() {
        val state = _uiState.value
        val media = state.inspectedMedia ?: return
        if (state.activeDownload != null) return

        viewModelScope.launch {
            val res = downloadEngine.downloadVideoCoverHd(
                media = media,
                settings = settings.value,
                customPrefix = state.customOptions.customFilePrefix,
                onProgress = { progress ->
                    _uiState.update { it.copy(activeDownload = progress) }
                }
            )
            res.fold(
                onSuccess = { saved ->
                    _uiState.update {
                        it.copy(
                            activeDownload = null,
                            statusMessage = "Đã lưu Ảnh Bìa HD Không Logo (${saved.resolution}) vào Kho lưu trữ & Thư viện!",
                            isStatusError = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            activeDownload = null,
                            statusMessage = "Không thể tải ảnh bìa: ${err.message}",
                            isStatusError = true
                        )
                    }
                }
            )
        }
    }

    fun downloadQuickAudioMp3() {
        val state = _uiState.value
        val media = state.inspectedMedia ?: return
        if (state.activeDownload != null) return
        val audioStream = media.streams.find { it.tier == VideoQualityTier.AUDIO_MP3 } ?: return

        viewModelScope.launch {
            val res = downloadEngine.downloadVideoOrAudio(
                media = media,
                stream = audioStream,
                settings = settings.value,
                customPrefix = state.customOptions.customFilePrefix,
                onProgress = { progress ->
                    _uiState.update { it.copy(activeDownload = progress) }
                }
            )
            res.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            activeDownload = null,
                            statusMessage = "Đã tách & lưu Nhạc nền MP3 320kbps (${media.musicTitle})!",
                            isStatusError = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            activeDownload = null,
                            statusMessage = "Lỗi tải nhạc MP3: ${err.message}",
                            isStatusError = true
                        )
                    }
                }
            )
        }
    }

    fun notifyCopiedToClipboard(label: String) {
        _uiState.update {
            it.copy(
                statusMessage = "Đã sao chép $label vào bộ nhớ tạm!",
                isStatusError = false
            )
        }
    }

    fun toggleBatchMode() {
        _uiState.update {
            it.copy(
                isBatchModeExpanded = !it.isBatchModeExpanded
            )
        }
    }

    fun onBatchInputChange(text: String) {
        _uiState.update { it.copy(batchInputText = text) }
    }

    fun startBatchDownload() {
        val rawBatch = _uiState.value.batchInputText
        val urls = extractorRepository.extractAllTikTokUrls(rawBatch)
        if (urls.isEmpty()) {
            _uiState.update {
                it.copy(
                    statusMessage = "Chưa tìm thấy liên kết TikTok hợp lệ trong danh sách tải hàng loạt.",
                    isStatusError = true
                )
            }
            return
        }
        if (_uiState.value.batchQueueProgress != null || _uiState.value.activeDownload != null) return

        viewModelScope.launch {
            val currentSettings = settings.value
            var successCount = 0

            urls.forEachIndexed { idx, url ->
                val step = idx + 1
                _uiState.update {
                    it.copy(
                        batchQueueProgress = BatchQueueProgress(
                            currentIndex = step,
                            totalLinks = urls.size,
                            currentUrl = url,
                            completedCount = successCount,
                            statusText = "Đang tải hàng loạt ($step/${urls.size}): $url"
                        )
                    )
                }

                val mediaRes = extractorRepository.inspectTikTokUrl(url)
                mediaRes.getOrNull()?.let { media ->
                    if (media.isPhotoSlideshow) {
                        val r = downloadEngine.downloadPhotoCarousel(
                            media = media,
                            selectedPhotos = media.photos,
                            includeAudioMp3 = currentSettings.includeMp3WithCarousel,
                            settings = currentSettings,
                            customPrefix = "Batch",
                            onProgress = { p ->
                                _uiState.update { it.copy(activeDownload = p) }
                            }
                        )
                        if (r.isSuccess) successCount++
                    } else {
                        val stream = media.streams.find { it.tier == currentSettings.defaultQualityTier }
                            ?: media.streams.firstOrNull()
                        if (stream != null) {
                            val r = downloadEngine.downloadVideoOrAudio(
                                media = media,
                                stream = stream,
                                settings = currentSettings,
                                customPrefix = "Batch",
                                onProgress = { p ->
                                    _uiState.update { it.copy(activeDownload = p) }
                                }
                            )
                            if (r.isSuccess) successCount++
                        }
                    }
                }
            }

            _uiState.update {
                it.copy(
                    activeDownload = null,
                    batchQueueProgress = null,
                    isBatchModeExpanded = false,
                    statusMessage = "Hoàn tất tải hàng loạt $successCount/${urls.size} liên kết TikTok Không Logo!",
                    isStatusError = false
                )
            }
        }
    }

    fun deleteDownloadItem(id: Long) {
        viewModelScope.launch {
            downloadDao.deleteById(id)
            _uiState.update {
                it.copy(
                    statusMessage = "Đã xoá tệp khỏi danh sách đã tải.",
                    isStatusError = false
                )
            }
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            downloadDao.clearAll()
            _uiState.update {
                it.copy(
                    statusMessage = "Đã dọn sạch lịch sử Kho lưu trữ.",
                    isStatusError = false
                )
            }
        }
    }

    fun openSettingsSheet(initialTab: Int = 0) {
        _uiState.update {
            it.copy(
                isSettingsSheetOpen = true,
                settingsInitialTab = initialTab
            )
        }
    }

    fun closeSettingsSheet() {
        _uiState.update { it.copy(isSettingsSheetOpen = false) }
    }

    fun openLivePreview(initialPhotoIndex: Int = 0) {
        val media = _uiState.value.inspectedMedia ?: return
        _uiState.update {
            it.copy(previewTarget = PreviewModalTarget.LiveMedia(media, initialPhotoIndex))
        }
    }

    fun openSavedDownloadPreview(item: DownloadItemEntity) {
        _uiState.update {
            it.copy(previewTarget = PreviewModalTarget.SavedDownload(item))
        }
    }

    fun closePreviewModal() {
        _uiState.update { it.copy(previewTarget = null) }
    }

    // Settings updates
    fun setDefaultQuality(tier: VideoQualityTier) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultQuality(tier)
            _uiState.update { state ->
                state.copy(
                    customOptions = state.customOptions.copy(selectedTier = tier)
                )
            }
        }
    }

    fun setForceNoWatermark(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateForceNoWatermark(enabled)
        }
    }

    fun setAutoDetectClipboard(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoDetectClipboard(enabled)
        }
    }

    fun setSaveToPublicGallery(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateSaveToPublicGallery(enabled)
        }
    }

    fun setIncludeMp3WithCarousel(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateIncludeMp3WithCarousel(enabled)
        }
    }

    fun setFileNamingRule(rule: FileNamingRule) {
        viewModelScope.launch {
            preferencesRepository.updateFileNamingRule(rule)
        }
    }

    fun setGitHubRepoSlug(repoSlug: String) {
        viewModelScope.launch {
            preferencesRepository.updateGitHubRepoSlug(repoSlug)
        }
    }

    fun setAutoCheckGitHubUpdate(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoCheckGitHubUpdate(enabled)
        }
    }

    fun openGitHubWorkflowDialog() {
        _uiState.update { it.copy(isGitHubWorkflowDialogOpen = true) }
    }

    fun closeGitHubWorkflowDialog() {
        _uiState.update { it.copy(isGitHubWorkflowDialogOpen = false) }
    }

    fun dismissUpdateBanner() {
        _uiState.update { it.copy(isUpdateBannerDismissed = true) }
    }

    fun checkForGitHubUpdates(repoOverride: String? = null, isStartupCheck: Boolean = false) {
        viewModelScope.launch {
            val currentSettings = settings.value
            if (isStartupCheck && !currentSettings.autoCheckGitHubUpdate) return@launch

            val effectiveVersion = preferencesRepository.getEffectiveVersionName(BuildConfig.VERSION_NAME)
            val rawRepo = if (!repoOverride.isNullOrBlank()) repoOverride else currentSettings.githubRepoSlug
            val targetRepo = if (rawRepo.isBlank() || rawRepo == "lamminhnhut/TikStudio-HD") {
                GitHubAutoUpdater.DEFAULT_GITHUB_REPO
            } else {
                rawRepo
            }
            if (!repoOverride.isNullOrBlank()) {
                preferencesRepository.updateGitHubRepoSlug(targetRepo)
            }

            val instantPreview = gitHubAutoUpdater.buildReadyReleasePreview(
                repoSlug = targetRepo,
                currentVersionName = effectiveVersion
            )
            _uiState.update {
                it.copy(
                    currentVersionName = effectiveVersion,
                    isCheckingGitHubUpdate = true,
                    githubReleaseInfo = it.githubReleaseInfo?.copy(
                        isNewerThanCurrent = gitHubAutoUpdater.isRemoteVersionNewer(
                            it.githubReleaseInfo.versionTag,
                            effectiveVersion
                        )
                    ) ?: instantPreview,
                    isUpdateBannerDismissed = if (isStartupCheck) it.isUpdateBannerDismissed else false
                )
            }

            val result = gitHubAutoUpdater.checkLatestRelease(
                rawRepoSlug = targetRepo,
                currentVersionName = effectiveVersion
            )
            result.fold(
                onSuccess = { release ->
                    val updatedEffectiveVersion = preferencesRepository.getEffectiveVersionName(BuildConfig.VERSION_NAME)
                    val isStillNewer = gitHubAutoUpdater.isRemoteVersionNewer(
                        release.versionTag,
                        updatedEffectiveVersion
                    )
                    val finalRelease = release.copy(isNewerThanCurrent = isStillNewer)
                    _uiState.update {
                        it.copy(
                            currentVersionName = updatedEffectiveVersion,
                            isCheckingGitHubUpdate = false,
                            githubReleaseInfo = finalRelease,
                            isUpdateBannerDismissed = if (isStillNewer) false else true,
                            statusMessage = if (isStartupCheck) {
                                it.statusMessage
                            } else if (isStillNewer) {
                                "Đã phát hiện bản cập nhật ${finalRelease.versionTag} từ GitHub Release!"
                            } else {
                                "Ứng dụng đã ở phiên bản mới nhất (${finalRelease.versionTag})!"
                            },
                            isStatusError = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            currentVersionName = effectiveVersion,
                            isCheckingGitHubUpdate = false,
                            githubReleaseInfo = instantPreview
                        )
                    }
                }
            )
        }
    }

    fun startAutoUpdateAndInstallApk() {
        val currentEffective = preferencesRepository.getEffectiveVersionName(BuildConfig.VERSION_NAME)
        val release = _uiState.value.githubReleaseInfo
            ?: gitHubAutoUpdater.buildReadyReleasePreview(settings.value.githubRepoSlug, currentEffective)

        viewModelScope.launch {
            val result = gitHubAutoUpdater.downloadReleaseApk(
                release = release,
                onProgress = { progress ->
                    _uiState.update {
                        it.copy(apkUpdateDownloadProgress = progress)
                    }
                }
            )

            result.fold(
                onSuccess = { apkFile ->
                    val updatedVersion = gitHubAutoUpdater.normalizeVersionTag(release.versionTag)
                    preferencesRepository.markVersionUpdated(updatedVersion)
                    gitHubAutoUpdater.installDownloadedApk(apkFile)
                    _uiState.update {
                        it.copy(
                            currentVersionName = updatedVersion,
                            githubReleaseInfo = release.copy(
                                versionTag = updatedVersion,
                                isNewerThanCurrent = false
                            ),
                            isUpdateBannerDismissed = true,
                            statusMessage = "Đã cập nhật thành công lên phiên bản $updatedVersion!",
                            isStatusError = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            apkUpdateDownloadProgress = null,
                            statusMessage = "Lỗi khi tải gói cập nhật APK: ${err.message}",
                            isStatusError = true
                        )
                    }
                }
            )
        }
    }

    fun installReadyApk() {
        val progress = _uiState.value.apkUpdateDownloadProgress ?: return
        val readyFile = progress.downloadedApkFile ?: return
        viewModelScope.launch {
            val updatedVersion = gitHubAutoUpdater.normalizeVersionTag(progress.versionTag)
            preferencesRepository.markVersionUpdated(updatedVersion)
            gitHubAutoUpdater.installDownloadedApk(readyFile)
            _uiState.update { state ->
                state.copy(
                    currentVersionName = updatedVersion,
                    githubReleaseInfo = state.githubReleaseInfo?.copy(
                        versionTag = updatedVersion,
                        isNewerThanCurrent = false
                    ),
                    isUpdateBannerDismissed = true,
                    statusMessage = "Đã cập nhật thành công lên phiên bản $updatedVersion!",
                    isStatusError = false
                )
            }
        }
    }

    fun shareDownloadedFile(context: Context, item: DownloadItemEntity) {
        try {
            val file = File(item.localFilePath)
            if (file.exists()) {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val mimeType = when (item.mediaType) {
                    "PHOTO" -> "image/jpeg"
                    "AUDIO" -> "audio/mpeg"
                    else -> "video/mp4"
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${item.title} (${item.qualityLabel} • Không Logo)"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, "Chia sẻ tệp Không Logo")
                )
            } else {
                val shareTextIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, item.remoteStreamUrl.ifBlank { item.sourceTikTokUrl })
                }
                context.startActivity(
                    Intent.createChooser(shareTextIntent, "Chia sẻ liên kết")
                )
            }
        } catch (_: Exception) {
            _uiState.update {
                it.copy(
                    statusMessage = "Đã sao chép đường dẫn luồng trực tiếp.",
                    isStatusError = false
                )
            }
        }
    }

    fun refreshServicesStatus(context: Context) {
        val overlayActive = FloatingTikTokShortcutService.isRunning
        val accessibilityActive = TikAutoScrollAccessibilityService.isAccessibilitySettingsEnabled(context)
        _uiState.update {
            it.copy(
                isFloatingShortcutActive = overlayActive,
                isAccessibilityServiceActive = accessibilityActive
            )
        }
    }

    fun toggleFloatingShortcut(context: Context): Boolean {
        val hasOverlay = FloatingTikTokShortcutService.canDrawOverlays(context)
        if (!hasOverlay) {
            return false
        }
        if (FloatingTikTokShortcutService.isRunning) {
            FloatingTikTokShortcutService.stop(context)
            _uiState.update { it.copy(isFloatingShortcutActive = false) }
            viewModelScope.launch { preferencesRepository.updateFloatingShortcutEnabled(false) }
        } else {
            FloatingTikTokShortcutService.start(context)
            _uiState.update { it.copy(isFloatingShortcutActive = true) }
            viewModelScope.launch { preferencesRepository.updateFloatingShortcutEnabled(true) }
        }
        return true
    }

    fun toggleAutoScroll() {
        TikAutoScrollAccessibilityService.toggleHandsFreeAutoScroll(
            settings.value.autoScrollIntervalSeconds
        )
    }

    fun triggerNextVideoSwipe() {
        TikAutoScrollAccessibilityService.swipeToNextVideo()
    }

    fun updateAutoSwipeAfterDownload(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateAutoSwipeAfterDownload(enabled) }
    }

    fun updateSwipeDelaySeconds(seconds: Int) {
        viewModelScope.launch { preferencesRepository.updateSwipeDelaySeconds(seconds) }
    }

    fun updateAutoScrollInterval(seconds: Int) {
        viewModelScope.launch { preferencesRepository.updateAutoScrollIntervalSeconds(seconds) }
    }

    fun updateAutoDownloadOnScroll(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateAutoDownloadOnScroll(enabled) }
    }

    fun launchTikTokApp(context: Context) {
        val tiktokPackages = listOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.ss.android.ugc.aweme"
        )
        val pm = context.packageManager
        for (pkg in tiktokPackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                if (FloatingTikTokShortcutService.canDrawOverlays(context)) {
                    FloatingTikTokShortcutService.start(context)
                }
                context.startActivity(launchIntent)
                return
            }
        }
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com"))
            context.startActivity(webIntent)
        } catch (_: Exception) {}
    }

    class Factory(
        private val extractorRepository: TikTokExtractorRepository,
        private val downloadEngine: MediaDownloadEngine,
        private val downloadDao: DownloadHistoryDao,
        private val preferencesRepository: StudioPreferencesRepository,
        private val gitHubAutoUpdater: GitHubAutoUpdater
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TikStudioViewModel(
                extractorRepository = extractorRepository,
                downloadEngine = downloadEngine,
                downloadDao = downloadDao,
                preferencesRepository = preferencesRepository,
                gitHubAutoUpdater = gitHubAutoUpdater
            ) as T
        }
    }
}
