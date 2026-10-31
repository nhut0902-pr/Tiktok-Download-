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

const val DEFAULT_GITHUB_REPO = "nhut0902-pr/Tiktok-Download-"

data class StudioSettings(
    val defaultQualityTier: VideoQualityTier = VideoQualityTier.FULL_HD_1080P,
    val forceNoWatermark: Boolean = true,
    val autoDetectClipboard: Boolean = true,
    val saveToPublicGallery: Boolean = true,
    val includeMp3WithCarousel: Boolean = true,
    val fileNamingRule: FileNamingRule = FileNamingRule.AUTHOR_ID_QUALITY,
    val githubRepoSlug: String = DEFAULT_GITHUB_REPO,
    val autoCheckGitHubUpdate: Boolean = true,
    val appliedUpdateVersion: String = ""
)

class StudioPreferencesRepository(private val context: Context) {

    companion object {
        const val DEFAULT_GITHUB_REPO = com.example.data.preferences.DEFAULT_GITHUB_REPO
    }

    private val syncPrefs by lazy {
        context.getSharedPreferences("tikstudio_update_sync", Context.MODE_PRIVATE)
    }

    private object Keys {
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality_tier")
        val FORCE_NO_WATERMARK = booleanPreferencesKey("force_no_watermark")
        val AUTO_DETECT_CLIPBOARD = booleanPreferencesKey("auto_detect_clipboard")
        val SAVE_TO_PUBLIC_GALLERY = booleanPreferencesKey("save_to_public_gallery")
        val INCLUDE_MP3_WITH_CAROUSEL = booleanPreferencesKey("include_mp3_with_carousel")
        val FILE_NAMING_RULE = stringPreferencesKey("file_naming_rule")
        val GITHUB_REPO_SLUG = stringPreferencesKey("github_repo_slug")
        val AUTO_CHECK_GITHUB_UPDATE = booleanPreferencesKey("auto_check_github_update")
        val APPLIED_UPDATE_VERSION = stringPreferencesKey("applied_update_version")
    }

    val settingsFlow: Flow<StudioSettings> = context.studioDataStore.data.map { prefs ->
        val storedRepo = prefs[Keys.GITHUB_REPO_SLUG]
        val effectiveRepo = if (storedRepo.isNullOrBlank() || storedRepo == "lamminhnhut/TikStudio-HD") {
            DEFAULT_GITHUB_REPO
        } else {
            storedRepo
        }
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
            githubRepoSlug = effectiveRepo,
            autoCheckGitHubUpdate = prefs[Keys.AUTO_CHECK_GITHUB_UPDATE] ?: true,
            appliedUpdateVersion = prefs[Keys.APPLIED_UPDATE_VERSION]
                ?: syncPrefs.getString("applied_update_version", "").orEmpty()
        )
    }

    fun getEffectiveVersionName(buildConfigVersion: String): String {
        val cleanBuild = buildConfigVersion.trim().removePrefix("v").removePrefix("V")
        val savedApplied = syncPrefs.getString("applied_update_version", "").orEmpty()
            .trim()
            .removePrefix("v")
            .removePrefix("V")
        if (savedApplied.isBlank()) return cleanBuild
        return if (isVersionAHigherOrEqual(savedApplied, cleanBuild)) savedApplied else cleanBuild
    }

    private fun isVersionAHigherOrEqual(versionA: String, versionB: String): Boolean {
        val aParts = versionA.split(".", "-").mapNotNull { it.toIntOrNull() }
        val bParts = versionB.split(".", "-").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(aParts.size, bParts.size)
        for (i in 0 until maxLen) {
            val a = aParts.getOrElse(i) { 0 }
            val b = bParts.getOrElse(i) { 0 }
            if (a > b) return true
            if (a < b) return false
        }
        return true
    }

    suspend fun markVersionUpdated(version: String) {
        val cleanVersion = version.trim().removePrefix("v").removePrefix("V")
        syncPrefs.edit().putString("applied_update_version", cleanVersion).apply()
        context.studioDataStore.edit { it[Keys.APPLIED_UPDATE_VERSION] = cleanVersion }
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
