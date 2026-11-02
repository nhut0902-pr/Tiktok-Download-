package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.R
import com.example.service.ScreenRecordQualityOption
import com.example.service.ScreenRecorderForegroundService
import com.example.service.ScreenRecorderState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.EmeraldBadgeBg
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioElevatedSurface
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private val RecorderOrange = Color(0xFFF97316)

@Composable
fun ScreenRecorderUtilitySection(
    recorderState: ScreenRecorderState,
    onInstallUtility: () -> Unit,
    onSelectQuality: (ScreenRecordQualityOption) -> Unit,
    onOpenQualityDialog: () -> Unit,
    onCloseQualityDialog: () -> Unit,
    onToggleFloatingBubble: (Boolean) -> Unit,
    onToggleRadialMenu: () -> Unit,
    onCaptureScreenshot: () -> Unit,
    onToggleBrushTool: () -> Unit,
    onToggleMic: (Boolean) -> Unit,
    onMicPermissionResult: (Boolean) -> Unit,
    onToggleAutoCloseOtherApps: (Boolean) -> Unit,
    onCloseOtherAppsNow: () -> Unit,
    onToggleAutoExitToBackground: (Boolean) -> Unit,
    onStartScreenRecording: (Int, android.content.Intent?, ScreenRecordQualityOption) -> Unit,
    onPauseResumeScreenRecording: () -> Unit,
    onStopScreenRecording: () -> Unit,
    onExitToBackgroundNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val hasOverlayPermission = recorderState.hasOverlayPermission ||
        ScreenRecorderForegroundService.canDrawOverlays(context)

    LaunchedEffect(hasAudioPermission) {
        onMicPermissionResult(hasAudioPermission)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        ScreenRecorderForegroundService.ensureFloatingOverlayRunning(context)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onMicPermissionResult(granted)
        if (granted) {
            onToggleMic(true)
        }
    }

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            onStartScreenRecording(result.resultCode, result.data, recorderState.selectedQuality)
            if (recorderState.autoExitToBackgroundOnRecord) {
                ScreenRecorderForegroundService.exitAppToBackgroundForRecording(context)
            }
        } else {
            onStartScreenRecording(Activity.RESULT_OK, result.data, recorderState.selectedQuality)
        }
    }

    val triggerRecordingWithCurrentQuality = { chosenQuality: ScreenRecordQualityOption ->
        onSelectQuality(chosenQuality)
        onCloseQualityDialog()
        if (recorderState.isMicEnabled && !hasAudioPermission) {
            try {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } catch (_: Exception) {}
        }
        requestScreenCaptureOrFallback(
            context = context,
            onLaunchIntent = { captureIntent ->
                try {
                    mediaProjectionLauncher.launch(captureIntent)
                } catch (_: Exception) {
                    onStartScreenRecording(Activity.RESULT_OK, null, chosenQuality)
                }
            },
            onFallbackStart = {
                onStartScreenRecording(Activity.RESULT_OK, null, chosenQuality)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(StudioElevatedSurface)
            .border(
                1.dp,
                if (recorderState.isRecording) NeonCoral.copy(alpha = 0.8f) else RecorderOrange.copy(alpha = 0.65f),
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
            .testTag("screen_recorder_utility_card")
    ) {
        // Header Row with Orange Camcorder SVG Logo & V1.0.3 Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_svg_recorder_orange_logo),
                    contentDescription = "Trình Ghi Màn Hình",
                    modifier = Modifier.size(40.dp)
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Trình Ghi Màn Hình",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(EmeraldBadgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("version_v103_badge")
                        ) {
                            Text(
                                text = "V1.0.4",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldBadge
                            )
                        }
                    }
                    Text(
                        text = "Nút nổi xòe vòng cung • Thanh thông báo • Quay 4K/Full HD 60FPS",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (recorderState.isRecording) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCoral.copy(alpha = 0.2f))
                        .border(1.dp, NeonCoral, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("recording_timer_badge")
                ) {
                    Text(
                        text = if (recorderState.isPaused) "⏸ ${recorderState.formattedElapsedTime}" else "● ${recorderState.formattedElapsedTime}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (recorderState.isPaused) GoldBadge else NeonCoral
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3-Step Guide Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioBackground)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Các bước sử dụng Trình Ghi Màn Hình V1.0.4:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RecorderOrange
            )
            UtilityStepItem(
                stepNumber = "1",
                text = "Tải và cài đặt tiện ích Trình Ghi Màn Hình về máy",
                isDone = recorderState.isUtilityInstalled
            )
            UtilityStepItem(
                stepNumber = "2",
                text = "Chọn chất lượng cao (4K Ultra HD / Full HD 1080p) + Mở Mic thu âm",
                isDone = recorderState.isUtilityInstalled && recorderState.isMicEnabled
            )
            UtilityStepItem(
                stepNumber = "3",
                text = "Cấp quyền Hiển thị trên ứng dụng khác để hiện Pop-up nổi & Thanh thông báo khi thoát app",
                isDone = recorderState.isUtilityInstalled && hasOverlayPermission
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!recorderState.isUtilityInstalled) {
            // MANDATORY UTILITY DOWNLOAD GATE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, RecorderOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yêu cầu tải xuống tiện ích trước khi sử dụng",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBadge
                    )
                    Text(
                        text = "4K 60FPS • H.264",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = RecorderOrange
                    )
                }

                Text(
                    text = "Muốn sử dụng tính năng Trình Ghi Màn Hình (Nút nổi cam xòe vòng cung & Thanh điều khiển thông báo), bạn phải tải xuống tiện ích về máy.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                if (recorderState.isInstallingUtility) {
                    LinearProgressIndicator(
                        progress = { recorderState.installProgressPct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RecorderOrange,
                        trackColor = StudioSurface
                    )
                }

                Button(
                    onClick = {
                        onInstallUtility()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasNotif = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasNotif) {
                                try {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RecorderOrange,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 11.dp, horizontal = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("install_screen_utility_button")
                ) {
                    SvgDrawableGraphic(
                        drawableResId = R.drawable.ic_svg_addon_download,
                        tint = Color.White,
                        size = 18.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tải xuống tiện ích Quay Màn Hình (V1.0.4)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // UNLOCKED CONTROLS AFTER DOWNLOADING UTILITY

            // OVERLAY PERMISSION BANNER (Critical so Floating Pop-up & Mini Bar appear outside the app!)
            if (!hasOverlayPermission) {
                Surface(
                    onClick = { ScreenRecorderForegroundService.requestOverlayPermission(context) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2E1605),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RecorderOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grant_overlay_permission_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ Bật quyền Hiển thị Pop-up Nổi trên màn hình nền",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldBadge
                            )
                            Text(
                                text = "Nhấn vào đây để bật 'Hiển thị trên ứng dụng khác' giúp Nút nổi cam & Bảng điều khiển Mini hiển thị khi bạn thoát app!",
                                fontSize = 10.sp,
                                color = Color.White,
                                lineHeight = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RecorderOrange)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Bật ngay",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 1. Live Notification Control Bar (Exact match to Screenshot 4: Record | Screenshot | Tools | Home | Exit)
            Text(
                text = "Thanh điều khiển thông báo & Nút nổi (Giống Trình Ghi Màn Hình):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            RecorderNotificationToolbarCard(
                recorderState = recorderState,
                onRecordClick = {
                    if (recorderState.isRecording) {
                        onStopScreenRecording()
                    } else {
                        onOpenQualityDialog()
                    }
                },
                onScreenshotClick = onCaptureScreenshot,
                onToolsClick = onOpenQualityDialog,
                onHomeClick = {
                    if (!hasOverlayPermission) {
                        ScreenRecorderForegroundService.requestOverlayPermission(context)
                    }
                    onToggleRadialMenu()
                },
                onExitClick = {
                    if (recorderState.isRecording) {
                        onStopScreenRecording()
                    } else {
                        onToggleFloatingBubble(false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Toggle for Floating Orange Camcorder Bubble & Radial Arc Fan-Out Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF191D26))
                    .border(1.dp, RecorderOrange.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_svg_recorder_orange_logo),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Column {
                        Text(
                            text = "Nút nổi cam & Menu xòe vòng cung",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (recorderState.isRadialMenuExpanded) {
                                "Đang mở menu xòe 5 nút & Bảng điều khiển Mini Pop-up"
                            } else {
                                "Hiển thị trên màn hình chính & các app khác khi thoát app"
                            },
                            fontSize = 10.sp,
                            color = RecorderOrange
                        )
                    }
                }

                Surface(
                    onClick = {
                        if (!hasOverlayPermission) {
                            ScreenRecorderForegroundService.requestOverlayPermission(context)
                        }
                        ScreenRecorderForegroundService.ensureFloatingOverlayRunning(context)
                        onToggleRadialMenu()
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (recorderState.isRadialMenuExpanded) RecorderOrange else RecorderOrange.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RecorderOrange),
                    modifier = Modifier.testTag("toggle_radial_menu_preview_button")
                ) {
                    Text(
                        text = if (recorderState.isRadialMenuExpanded) "Thu gọn X" else "Mở Menu Xòe",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (recorderState.isRadialMenuExpanded) Color.White else RecorderOrange,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Video Quality Selector Row (4K Ultra HD, 2K QHD, Full HD 1080p, HD 720p)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chất lượng quay video:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${recorderState.selectedQuality.shortLabel} (${recorderState.selectedQuality.resolutionText} • 60FPS)",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldBadge
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScreenRecordQualityOption.entries.forEach { option ->
                    val selected = recorderState.selectedQuality == option
                    Surface(
                        onClick = { onSelectQuality(option) },
                        shape = RoundedCornerShape(9.dp),
                        color = if (selected) RecorderOrange.copy(alpha = 0.2f) else StudioBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selected) RecorderOrange else StudioCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quality_chip_${option.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = option.shortLabel.replace("Ultra HD", "").replace("1080p", "").replace("720p", "").trim(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selected) RecorderOrange else TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "${option.heightPx}p 60f",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                color = if (selected) EmeraldBadge else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recorderState.statusMessage,
                    fontSize = 11.sp,
                    color = if (recorderState.isRecording) NeonCoral else EmeraldBadge,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = onInstallUtility,
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldBadgeBg,
                    modifier = Modifier.testTag("install_screen_utility_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = R.drawable.ic_svg_check_circle,
                            tint = EmeraldBadge,
                            size = 12.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đã tải tiện ích",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldBadge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Control Toggles: Microphone + Auto-Close Other Apps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = {
                        val nextState = !recorderState.isMicEnabled
                        if (nextState && !hasAudioPermission) {
                            onToggleMic(true)
                            try {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } catch (_: Exception) {}
                        } else {
                            onToggleMic(nextState)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (recorderState.isMicEnabled) EmeraldBadge.copy(alpha = 0.16f) else StudioBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (recorderState.isMicEnabled) EmeraldBadge else StudioCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_mic_recorder_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = if (recorderState.isMicEnabled) R.drawable.ic_svg_mic_on else R.drawable.ic_svg_mic_off,
                            tint = if (recorderState.isMicEnabled) EmeraldBadge else TextSecondary,
                            size = 15.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (recorderState.isMicEnabled) "Mở Mic: ĐANG BẬT" else "Mở Mic: ĐANG TẮT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (recorderState.isMicEnabled) EmeraldBadge else TextSecondary
                        )
                    }
                }

                Surface(
                    onClick = onCloseOtherAppsNow,
                    shape = RoundedCornerShape(10.dp),
                    color = StudioBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("close_other_apps_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = R.drawable.ic_svg_clean_ram,
                            tint = ElectricCyan,
                            size = 15.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Thoát các app khác",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Switch for Auto-Close Other Apps when opening TikStudio HD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioBackground)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mở app tự động thoát các app khác nhau",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Giải phóng RAM để quay video ${recorderState.selectedQuality.shortLabel} 60FPS mượt mà",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = recorderState.autoCloseOtherAppsOnOpen,
                    onCheckedChange = onToggleAutoCloseOtherApps,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF001F24),
                        checkedTrackColor = EmeraldBadge
                    ),
                    modifier = Modifier.testTag("switch_auto_close_other_apps")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary CTA Row: Start (opens Quality Selector Pop-up) / Stop + Exit App to Background
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!recorderState.isRecording) {
                    Button(
                        onClick = onOpenQualityDialog,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RecorderOrange,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 11.dp, horizontal = 12.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .testTag("start_screen_recording_button")
                    ) {
                        SvgRecordDotGraphic(
                            color = Color.White,
                            size = 15.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quay Video (${recorderState.selectedQuality.shortLabel})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Button(
                        onClick = onStopScreenRecording,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCoral,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 11.dp, horizontal = 12.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .testTag("stop_screen_recording_button")
                    ) {
                        SvgStopSquareGraphic(
                            color = Color.White,
                            size = 16.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dừng & Lưu (${recorderState.formattedElapsedTime})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Exit App to Background & Record Other Apps Button
                Surface(
                    onClick = {
                        if (!hasOverlayPermission) {
                            ScreenRecorderForegroundService.requestOverlayPermission(context)
                        }
                        if (!recorderState.isRecording) {
                            requestScreenCaptureOrFallback(
                                context = context,
                                onLaunchIntent = { captureIntent ->
                                    try {
                                        mediaProjectionLauncher.launch(captureIntent)
                                    } catch (_: Exception) {
                                        onExitToBackgroundNow()
                                    }
                                },
                                onFallbackStart = {
                                    onExitToBackgroundNow()
                                }
                            )
                        } else {
                            onExitToBackgroundNow()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = StudioBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBadge.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exit_to_background_record_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 11.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = R.drawable.ic_svg_exit_bg,
                            tint = EmeraldBadge,
                            size = 15.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Chạy nền quay App khác",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = EmeraldBadge,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    // HIGH-QUALITY VIDEO SELECTION DIALOG (Shown when clicking Record / Tools)
    if (recorderState.isQualityDialogOpen) {
        Dialog(onDismissRequest = onCloseQualityDialog) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(StudioSurface)
                    .border(1.5.dp, RecorderOrange, RoundedCornerShape(20.dp))
                    .padding(18.dp)
                    .testTag("quality_selector_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_svg_recorder_orange_logo),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                    Column {
                        Text(
                            text = "Chọn Chất Lượng Quay Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tự động hiện Pop-up điều khiển nổi & chạy nền quay các app khác",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                ScreenRecordQualityOption.entries.forEach { option ->
                    val isSelected = recorderState.selectedQuality == option
                    Surface(
                        onClick = { onSelectQuality(option) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) RecorderOrange.copy(alpha = 0.16f) else StudioElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) RecorderOrange else StudioCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_quality_option_${option.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.fullTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) RecorderOrange else TextPrimary
                                )
                                Text(
                                    text = "Độ phân giải: ${option.resolutionText} • Bitrate: ${option.bitrateMbps} Mbps",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                SvgDrawableGraphic(
                                    drawableResId = R.drawable.ic_svg_check_circle,
                                    tint = RecorderOrange,
                                    size = 18.dp
                                )
                            }
                        }
                    }
                }

                // Auto-Exit to Background Toggle inside Dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioBackground)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tự động thoát ra chạy nền khi bắt đầu quay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldBadge
                        )
                        Text(
                            text = "Hiện Pop-up điều khiển mini trên màn hình chính & các app khác",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = recorderState.autoExitToBackgroundOnRecord,
                        onCheckedChange = onToggleAutoExitToBackground,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF001F24),
                            checkedTrackColor = EmeraldBadge
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onCloseQualityDialog,
                        shape = RoundedCornerShape(12.dp),
                        color = StudioElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đóng",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { triggerRecordingWithCurrentQuality(recorderState.selectedQuality) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RecorderOrange,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 11.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("confirm_quality_start_recording_button")
                    ) {
                        SvgRecordDotGraphic(color = Color.White, size = 14.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bắt đầu quay ${recorderState.selectedQuality.shortLabel}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exact Notification-Style Horizontal Control Bar matching Screenshot 4:
 * [Orange Camcorder Icon] | Record | Screenshot | Tools | Home | Exit
 */
@Composable
fun RecorderNotificationToolbarCard(
    recorderState: ScreenRecorderState,
    onRecordClick: () -> Unit,
    onScreenshotClick: () -> Unit,
    onToolsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF262930))
            .border(1.dp, Color(0xFF3F4451), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("xrecorder_notification_toolbar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Orange Rounded-Square Camcorder Logo
        Image(
            painter = painterResource(id = R.drawable.ic_svg_recorder_orange_logo),
            contentDescription = "Trình Ghi Màn Hình",
            modifier = Modifier
                .size(42.dp)
                .clickable { onHomeClick() }
        )

        Spacer(modifier = Modifier.width(4.dp))

        // 1. Record / Stop
        NotificationBarActionColumn(
            label = if (recorderState.isRecording) recorderState.formattedElapsedTime else "Record",
            onClick = onRecordClick,
            testTag = "notif_bar_btn_record",
            modifier = Modifier.weight(1f)
        ) {
            if (recorderState.isRecording) {
                SvgStopSquareGraphic(color = Color(0xFFEF4444), size = 22.dp)
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_svg_notif_record),
                    contentDescription = "Record",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 2. Screenshot
        NotificationBarActionColumn(
            label = "Screenshot",
            onClick = onScreenshotClick,
            testTag = "notif_bar_btn_screenshot",
            modifier = Modifier.weight(1f)
        ) {
            SvgDrawableGraphic(
                drawableResId = R.drawable.ic_svg_notif_screenshot,
                tint = RecorderOrange,
                size = 24.dp
            )
        }

        // 3. Tools
        NotificationBarActionColumn(
            label = "Tools",
            onClick = onToolsClick,
            testTag = "notif_bar_btn_tools",
            modifier = Modifier.weight(1f)
        ) {
            SvgDrawableGraphic(
                drawableResId = R.drawable.ic_svg_notif_tools,
                tint = RecorderOrange,
                size = 24.dp
            )
        }

        // 4. Home
        NotificationBarActionColumn(
            label = "Home",
            onClick = onHomeClick,
            testTag = "notif_bar_btn_home",
            modifier = Modifier.weight(1f)
        ) {
            SvgDrawableGraphic(
                drawableResId = R.drawable.ic_svg_notif_home,
                tint = RecorderOrange,
                size = 24.dp
            )
        }

        // 5. Exit
        NotificationBarActionColumn(
            label = "Exit",
            onClick = onExitClick,
            testTag = "notif_bar_btn_exit",
            modifier = Modifier.weight(1f)
        ) {
            SvgDrawableGraphic(
                drawableResId = R.drawable.ic_svg_notif_exit,
                tint = RecorderOrange,
                size = 24.dp
            )
        }
    }
}

@Composable
private fun NotificationBarActionColumn(
    label: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        iconContent()
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Floating Mini Pop-up Control Bar shown while recording or paused so the user can
 * Pause/Resume, toggle Mic, take a Screenshot, Switch to other apps, or Stop & Save.
 */
@Composable
fun ScreenRecorderMiniPopupBar(
    recorderState: ScreenRecorderState,
    onPauseResume: () -> Unit,
    onToggleMic: () -> Unit,
    onScreenshot: () -> Unit,
    onSwitchToOtherApp: () -> Unit,
    onStopAndSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xF5191D26),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (recorderState.isPaused) GoldBadge else RecorderOrange
        ),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("screen_recorder_mini_popup")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_svg_recorder_orange_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (recorderState.isPaused) {
                            "TẠM DỪNG • ${recorderState.selectedQuality.shortLabel}"
                        } else {
                            "ĐANG QUAY ${recorderState.selectedQuality.shortLabel}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (recorderState.isPaused) GoldBadge.copy(alpha = 0.2f)
                            else NeonCoral.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = recorderState.formattedElapsedTime,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (recorderState.isPaused) GoldBadge else NeonCoral
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Pause / Resume
                Surface(
                    onClick = onPauseResume,
                    shape = RoundedCornerShape(10.dp),
                    color = StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (recorderState.isPaused) EmeraldBadge else GoldBadge
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mini_popup_pause_resume_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgPlayPauseGraphic(
                            isPlaying = !recorderState.isPaused,
                            color = if (recorderState.isPaused) EmeraldBadge else GoldBadge,
                            size = 13.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (recorderState.isPaused) "Tiếp tục" else "Tạm dừng",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (recorderState.isPaused) EmeraldBadge else GoldBadge
                        )
                    }
                }

                // 2. Mic Toggle
                Surface(
                    onClick = onToggleMic,
                    shape = RoundedCornerShape(10.dp),
                    color = StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier
                        .weight(0.85f)
                        .testTag("mini_popup_mic_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = if (recorderState.isMicEnabled) R.drawable.ic_svg_mic_on else R.drawable.ic_svg_mic_off,
                            tint = if (recorderState.isMicEnabled) EmeraldBadge else TextSecondary,
                            size = 13.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (recorderState.isMicEnabled) "Mic" else "Tắt",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (recorderState.isMicEnabled) EmeraldBadge else TextSecondary
                        )
                    }
                }

                // 3. Screenshot
                Surface(
                    onClick = onScreenshot,
                    shape = RoundedCornerShape(10.dp),
                    color = StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RecorderOrange.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(0.85f)
                        .testTag("mini_popup_screenshot_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = R.drawable.ic_svg_notif_screenshot,
                            tint = RecorderOrange,
                            size = 13.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chụp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RecorderOrange
                        )
                    }
                }

                // 4. Switch App
                Surface(
                    onClick = onSwitchToOtherApp,
                    shape = RoundedCornerShape(10.dp),
                    color = StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mini_popup_switch_app_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = R.drawable.ic_svg_exit_bg,
                            tint = ElectricCyan,
                            size = 13.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "App khác",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }

                // 5. Stop & Save
                Surface(
                    onClick = onStopAndSave,
                    shape = RoundedCornerShape(10.dp),
                    color = NeonCoral,
                    modifier = Modifier
                        .weight(0.95f)
                        .testTag("mini_popup_stop_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgStopSquareGraphic(color = Color.White, size = 13.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lưu",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UtilityStepItem(
    stepNumber: String,
    text: String,
    isDone: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isDone) EmeraldBadgeBg else StudioElevatedSurface),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                SvgDrawableGraphic(
                    drawableResId = R.drawable.ic_svg_check_circle,
                    tint = EmeraldBadge,
                    size = 12.dp
                )
            } else {
                Text(
                    text = stepNumber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RecorderOrange
                )
            }
        }
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFFE2E8F0),
            lineHeight = 15.sp
        )
    }
}

private fun requestScreenCaptureOrFallback(
    context: Context,
    onLaunchIntent: (android.content.Intent) -> Unit,
    onFallbackStart: () -> Unit
) {
    try {
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        val intent = manager?.createScreenCaptureIntent()
        if (intent != null) {
            onLaunchIntent(intent)
        } else {
            onFallbackStart()
        }
    } catch (_: Exception) {
        onFallbackStart()
    }
}
