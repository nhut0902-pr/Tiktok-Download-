package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.VideoQualityTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tikstudio_preferences"
)

enum class FileNamingRule(val id: String, val label: String, val example: String) {
    AUTHOR_ID_QUALITY(
        "AUTHOR_ID_QUALITY",
        "Tác giả + ID + Độ phân giải",
        "TikStudio_@creator_73981_1080p_NoLogo.mp4"
    ),
    TITLE_QUALITY(
        "TITLE_QUALITY",
        "Tiêu đề video + Độ phân giải",
        "anh_hoang_hon_phu_quoc_1080p_NoLogo.mp4"
    ),
    TIMESTAMP_CLEAN(
        "TIMESTAMP_CLEAN",
        "Mã thời gian gọn gàng",
        "TikStudio_20261029_2145_FullHD.mp4"
    );

    companion object {
        fun fromId(id: String): FileNamingRule =
            entries.find { it.id == id } ?: AUTHOR_ID_QUALITY
    }
}

data class StudioSettings(
    val defaultQualityTier: VideoQualityTier = VideoQualityTier.FULL_HD_1080P,
    val forceNoWatermark: Boolean = true,
    val autoDetectClipboard: Boolean = true,
    val saveToPublicGallery: Boolean = true,
    val includeMp3WithCarousel: Boolean = true,
    val fileNamingRule: FileNamingRule = FileNamingRule.AUTHOR_ID_QUALITY,
    val githubRepoSlug: String = "lamminhnhut/TikStudio-HD",
    val autoCheckGitHubUpdate: Boolean = true
)

class StudioPreferencesRepository(private val context: Context) {

    private object Keys {
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality_tier")
        val FORCE_NO_WATERMARK = booleanPreferencesKey("force_no_watermark")
        val AUTO_DETECT_CLIPBOARD = booleanPreferencesKey("auto_detect_clipboard")
        val SAVE_TO_PUBLIC_GALLERY = booleanPreferencesKey("save_to_public_gallery")
        val INCLUDE_MP3_WITH_CAROUSEL = booleanPreferencesKey("include_mp3_with_carousel")
        val FILE_NAMING_RULE = stringPreferencesKey("file_naming_rule")
        val GITHUB_REPO_SLUG = stringPreferencesKey("github_repo_slug")
        val AUTO_CHECK_GITHUB_UPDATE = booleanPreferencesKey("auto_check_github_update")
    }

    val settingsFlow: Flow<StudioSettings> = context.studioDataStore.data.map { prefs ->
        StudioSettings(
            defaultQualityTier = VideoQualityTier.fromId(
                prefs[Keys.DEFAULT_QUALITY] ?: VideoQualityTier.FULL_HD_1080P.id
            ),
            forceNoWatermark = prefs[Keys.FORCE_NO_WATERMARK] ?: true,
            autoDetectClipboard = prefs[Keys.AUTO_DETECT_CLIPBOARD] ?: true,
            saveToPublicGallery = prefs[Keys.SAVE_TO_PUBLIC_GALLERY] ?: true,
            includeMp3WithCarousel = prefs[Keys.INCLUDE_MP3_WITH_CAROUSEL] ?: true,
            fileNamingRule = FileNamingRule.fromId(
                prefs[Keys.FILE_NAMING_RULE] ?: FileNamingRule.AUTHOR_ID_QUALITY.id
            ),
            githubRepoSlug = prefs[Keys.GITHUB_REPO_SLUG] ?: "lamminhnhut/TikStudio-HD",
            autoCheckGitHubUpdate = prefs[Keys.AUTO_CHECK_GITHUB_UPDATE] ?: true
        )
    }

    suspend fun updateDefaultQuality(tier: VideoQualityTier) {
        context.studioDataStore.edit { it[Keys.DEFAULT_QUALITY] = tier.id }
    }

    suspend fun updateForceNoWatermark(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.FORCE_NO_WATERMARK] = enabled }
    }

    suspend fun updateAutoDetectClipboard(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.AUTO_DETECT_CLIPBOARD] = enabled }
    }

    suspend fun updateSaveToPublicGallery(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.SAVE_TO_PUBLIC_GALLERY] = enabled }
    }

    suspend fun updateIncludeMp3WithCarousel(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.INCLUDE_MP3_WITH_CAROUSEL] = enabled }
    }

    suspend fun updateFileNamingRule(rule: FileNamingRule) {
        context.studioDataStore.edit { it[Keys.FILE_NAMING_RULE] = rule.id }
    }

    suspend fun updateGitHubRepoSlug(repoSlug: String) {
        context.studioDataStore.edit { it[Keys.GITHUB_REPO_SLUG] = repoSlug.trim() }
    }

    suspend fun updateAutoCheckGitHubUpdate(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.AUTO_CHECK_GITHUB_UPDATE] = enabled }
    }
}
