package com.example

import com.example.data.update.GitHubAutoUpdater
import com.example.service.ScreenRecordQualityOption
import com.example.service.ScreenRecorderForegroundService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun screenRecorderUtility_v1_0_3_radialMenuAnd4kQuality_isConfigured() = runBlocking {
        ScreenRecorderForegroundService.installUtilityAddon()
        ScreenRecorderForegroundService.selectRecordingQuality(ScreenRecordQualityOption.UHD_4K)
        ScreenRecorderForegroundService.toggleRadialMenuExpanded(true)
        ScreenRecorderForegroundService.toggleBrushTool()

        val state = ScreenRecorderForegroundService.recorderState.value
        assertTrue(state.isUtilityInstalled)
        assertTrue(state.isFloatingBubbleEnabled)
        assertTrue(state.isRadialMenuExpanded)
        assertTrue(state.isBrushToolActive)
        assertEquals(ScreenRecordQualityOption.UHD_4K, state.selectedQuality)
        assertEquals(2160, state.selectedQuality.widthPx)
        assertEquals(3840, state.selectedQuality.heightPx)
        assertTrue(state.isMicEnabled)
        assertTrue(state.autoCloseOtherAppsOnOpen)
        assertEquals("00:00", state.formattedElapsedTime)
        assertTrue(GitHubAutoUpdater.WORKFLOW_YAML_TEMPLATE.contains("debug.keystore.base64"))
    }
}
