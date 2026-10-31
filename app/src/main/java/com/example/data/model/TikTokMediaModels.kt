package com.example.data.model

enum class MediaType(val displayLabel: String) {
    VIDEO("Video"),
    PHOTO_SLIDESHOW("Bộ Ảnh"),
    AUDIO("Nhạc MP3")
}

enum class VideoQualityTier(
    val id: String,
    val shortTitle: String,
    val subtitle: String,
    val badgeText: String,
    val resolution: String,
    val isNoWatermark: Boolean
) {
    FULL_HD_1080P(
        id = "FULL_HD_1080P",
        shortTitle = "Full HD 1080p",
        subtitle = "Không logo • Nét cao nhất • 60fps",
        badgeText = "FULL HD • NO LOGO",
        resolution = "1080x1920",
        isNoWatermark = true
    ),
    HD_720P(
        id = "HD_720P",
        shortTitle = "HD 720p",
        subtitle = "Không logo • Tải siêu tốc • Tiết kiệm dung lượng",
        badgeText = "HD 720P • NO LOGO",
        resolution = "720x1280",
        isNoWatermark = true
    ),
    AUDIO_MP3(
        id = "AUDIO_MP3",
        shortTitle = "Nhạc MP3 Gốc",
        subtitle = "Chỉ trích xuất âm thanh 320kbps",
        badgeText = "MP3 • 320KBPS",
        resolution = "Audio 320k",
        isNoWatermark = true
    ),
    WATERMARK_ORIGINAL(
        id = "WATERMARK_ORIGINAL",
        shortTitle = "Bản Có Logo",
        subtitle = "Giữ nguyên ID TikTok của tác giả",
        badgeText = "CÓ LOGO TIKTOK",
        resolution = "540x960",
        isNoWatermark = false
    );

    companion object {
        fun fromId(id: String): VideoQualityTier =
            entries.find { it.id == id } ?: FULL_HD_1080P
    }
}

data class QualityStreamOption(
    val tier: VideoQualityTier,
    val streamUrl: String,
    val fileSizeBytes: Long,
    val bitrateKbps: Int,
    val fps: Int,
    val codecLabel: String
)

data class PhotoSlideItem(
    val index: Int,
    val imageUrl: String,
    val width: Int = 1080,
    val height: Int = 1440,
    val estimatedSizeBytes: Long = 1_350_000L
)

data class TikTokMediaInfo(
    val id: String,
    val originalUrl: String,
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val coverUrl: String,
    val durationSeconds: Int,
    val playCount: Long,
    val likeCount: Long,
    val musicTitle: String,
    val musicAuthor: String,
    val musicUrl: String,
    val isPhotoSlideshow: Boolean,
    val streams: List<QualityStreamOption>,
    val photos: List<PhotoSlideItem>,
    val sourceEngine: String
) {
    val extractedHashtags: List<String>
        get() {
            val regex = Regex("#[\\p{L}0-9_]+")
            val found = regex.findAll(title).map { it.value }.distinct().toList()
            return if (found.isNotEmpty()) {
                found
            } else {
                listOf("#tiktokvn", "#xuhuong", "#fyp", "#fullhd", "#trending")
            }
        }

    fun buildViralRepostCaption(): String {
        val cleanTitle = title.replace(Regex("#[\\p{L}0-9_]+"), "").trim()
        val tags = (extractedHashtags + listOf("#xuhuong", "#fyp", "#viral")).distinct().joinToString(" ")
        return buildString {
            if (cleanTitle.isNotBlank()) {
                appendLine(cleanTitle)
                appendLine()
            }
            appendLine("🎬 Nguồn: $authorName ($authorHandle)")
            appendLine("🎵 Nhạc nền: $musicTitle - $musicAuthor")
            append(tags)
        }
    }
}

data class CustomDownloadOptions(
    val selectedTier: VideoQualityTier = VideoQualityTier.FULL_HD_1080P,
    val customFilePrefix: String = "",
    val includeAudioWithPhotos: Boolean = true,
    val force60FpsMetadata: Boolean = true,
    val selectedPhotoIndices: Set<Int> = emptySet()
)

data class ActiveDownloadProgress(
    val taskTitle: String,
    val qualityBadge: String,
    val progress: Float, // 0f..1f
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedKbps: Int,
    val statusText: String
)

data class BatchQueueProgress(
    val currentIndex: Int,
    val totalLinks: Int,
    val currentUrl: String,
    val completedCount: Int,
    val statusText: String
)

data class VaultStorageSummary(
    val totalFiles: Int = 0,
    val totalBytes: Long = 0L,
    val videoCount: Int = 0,
    val photoCount: Int = 0,
    val audioCount: Int = 0,
    val favoriteCount: Int = 0
)

data class SampleTikTokLink(
    val label: String,
    val tag: String,
    val url: String,
    val isPhotoCarousel: Boolean
)

data class ValidatedThumbnailPreview(
    val validatedUrl: String,
    val videoId: String,
    val coverImageUrl: String,
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val durationSeconds: Int,
    val isPhotoSlideshow: Boolean,
    val photoCount: Int = 0,
    val resolutionBadge: String = "FULL HD 1080P • NO LOGO"
)
