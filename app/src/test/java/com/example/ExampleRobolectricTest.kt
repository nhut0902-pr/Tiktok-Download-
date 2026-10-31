package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.VideoQualityTier
import com.example.data.network.TikTokExtractorRepository
import com.example.data.update.GitHubAutoUpdater
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read app name and verify TikStudio extractor and GitHub updater`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TikStudio HD", appName)

        val repo = TikTokExtractorRepository()
        val extracted = repo.extractTikTokUrlFromClipboard(
            "Xem ngay video cực nét https://vt.tiktok.com/ZS8N9vK2p/ nhé!"
        )
        assertEquals("https://vt.tiktok.com/ZS8N9vK2p/", extracted)
        assertNull(repo.extractTikTokUrlFromClipboard("Hello world 12345"))

        // Verify immediate thumbnail preview resolution upon URL validation
        val immediatePreview = repo.getImmediateThumbnailPreview(
            "https://www.tiktok.com/@cinema.vietnam/video/7398451029384719632"
        )
        assertNotNull(immediatePreview)
        assertTrue(immediatePreview!!.coverImageUrl.isNotBlank())

        // Verify Full HD 1080p & HD 720p No-Watermark stream resolution + Hashtag/Viral script extraction
        val videoInfo = repo.inspectTikTokUrl("https://www.tiktok.com/@cinema.vietnam/video/7398451029384719632").getOrThrow()
        assertTrue(videoInfo.streams.any { it.tier == VideoQualityTier.FULL_HD_1080P && it.tier.isNoWatermark })
        assertTrue(videoInfo.streams.any { it.tier == VideoQualityTier.HD_720P && it.tier.isNoWatermark })
        assertTrue(videoInfo.extractedHashtags.isNotEmpty())
        assertTrue(videoInfo.buildViralRepostCaption().contains(videoInfo.authorHandle))

        // Verify Batch Multi-Link URL extraction
        val batchUrls = repo.extractAllTikTokUrls(
            "Link 1: https://vt.tiktok.com/ZS8N9vK2p/\nLink 2: https://www.tiktok.com/@cinema.vietnam/video/7398451029384719632"
        )
        assertEquals(2, batchUrls.size)

        // Verify GitHub version 1.0.3 comparison and Gradle 9.3.1 in workflow template
        val updater = GitHubAutoUpdater(context)
        assertEquals("1.0.3", GitHubAutoUpdater.TARGET_UPDATE_VERSION)
        assertEquals("1.0.3", updater.normalizeVersionTag("v1.0.3"))
        assertEquals("nhut0902-pr/Tiktok-Download-", updater.normalizeRepoSlug(""))
        assertEquals("nhut0902-pr/Tiktok-Download-", updater.normalizeRepoSlug("lamminhnhut/TikStudio-HD"))
        assertTrue(updater.isRemoteVersionNewer("1.0.3", "1.0.2"))
        org.junit.Assert.assertFalse(updater.isRemoteVersionNewer("1.0.3", "1.0.3"))
        org.junit.Assert.assertFalse(updater.isRemoteVersionNewer("v1.0.3", "1.0.3"))
        assertTrue(GitHubAutoUpdater.WORKFLOW_YAML_TEMPLATE.contains("9.3.1"))
    }

    @Test
    fun `automatically detects and pastes TikTok URL from clipboard and shows Coil thumbnail preview`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val targetTikTokUrl = "https://www.tiktok.com/@cinema.vietnam/video/7398451029384719632"
        clipboard.setPrimaryClip(
            ClipData.newPlainText("TikTok Share", "Video cực đẹp $targetTikTokUrl")
        )

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onWindowFocusChanged(true)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("url_input_field")
            .assertTextContains(targetTikTokUrl)

        composeTestRule.onNodeWithTag("url_valid_badge")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("url_thumbnail_preview")
            .assertIsDisplayed()
    }

    @Test
    fun `displays Coil thumbnail preview immediately and opens unified Settings and GitHub Update tab`() {
        val sampleUrl = "https://vt.tiktok.com/ZS8N9vK2p/"
        composeTestRule.onNodeWithTag("url_input_field")
            .performTextReplacement(sampleUrl)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("url_valid_badge")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("url_thumbnail_preview")
            .assertIsDisplayed()

        // Open unified Settings button and switch to Update & GitHub tab
        composeTestRule.onNodeWithTag("settings_button")
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_tab_1")
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("github_workflow_dialog")
            .assertIsDisplayed()
    }
}
