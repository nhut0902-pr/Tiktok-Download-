package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.preferences.StudioSettings
import com.example.service.FloatingTikTokShortcutService
import com.example.service.TikAutoScrollAccessibilityService
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.EmeraldBadgeBg
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioElevatedSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TikTokShortcutFeatureSection(
    settings: StudioSettings,
    isUtilityDownloaded: Boolean,
    isFloatingShortcutActive: Boolean,
    isAccessibilityActive: Boolean,
    isAutoScrolling: Boolean,
    autoScrollCountdown: Int,
    onDownloadUtility: () -> Unit,
    onToggleFloatingShortcut: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onTestSwipeNext: () -> Unit,
    onLaunchTikTok: () -> Unit,
    onOpenSettingsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasOverlayPermission = FloatingTikTokShortcutService.canDrawOverlays(context)
    val hasAccessibilityPermission = TikAutoScrollAccessibilityService.isAccessibilitySettingsEnabled(context)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(StudioElevatedSurface)
            .border(
                1.dp,
                if (isFloatingShortcutActive) ElectricCyan.copy(alpha = 0.6f) else StudioCardBorder,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
            .testTag("tiktok_shortcut_feature_card")
    ) {
        // Header Row: SVG Graphic, Title & Badge
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    SvgDrawableGraphic(
                        drawableResId = R.drawable.ic_svg_tiktok_shortcut,
                        tint = ElectricCyan,
                        size = 20.dp
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tiện ích Phím tắt TikTok & Tự động lướt",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "V1.0.4",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricCyan
                            )
                        }
                    }
                    Text(
                        text = "Tải video trực tiếp trong app TikTok & tự động chuyển video",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isUtilityDownloaded) {
            // REQUIRED DOWNLOAD GATE BEFORE USING THIS UTILITY
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gói tiện ích chưa được tải về máy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBadge
                    )
                    Text(
                        text = "2.4 MB • Auto-Scroll",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = ElectricCyan
                    )
                }

                Text(
                    text = "Muốn sử dụng tính năng tải video bằng phím tắt nổi trong TikTok và tự động lướt chuyển video, bạn cần tải xuống tiện ích này về máy.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                Button(
                    onClick = onDownloadUtility,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF001F24)
                    ),
                    contentPadding = PaddingValues(vertical = 11.dp, horizontal = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_tiktok_utility_button")
                ) {
                    SvgDrawableGraphic(
                        drawableResId = R.drawable.ic_svg_addon_download,
                        tint = Color(0xFF001F24),
                        size = 18.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tải xuống tiện ích Phím tắt TikTok (V1.0.4)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // UNLOCKED CONTROLS AFTER UTILITY IS DOWNLOADED
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
                    text = "✅ Đã tải & kích hoạt tiện ích Phím tắt TikTok V1.0.4",
                    fontSize = 11.sp,
                    color = EmeraldBadge,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = onDownloadUtility,
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldBadgeBg,
                    modifier = Modifier.testTag("download_tiktok_utility_button")
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

            // Permission Status Badges Row (Using SVG)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPermissionChip(
                    drawableResId = R.drawable.ic_svg_tab_utilities,
                    label = if (hasOverlayPermission) "Bong bóng nổi: Đã cấp" else "Bong bóng: Cần cấp quyền",
                    isGranted = hasOverlayPermission,
                    onClick = {
                        if (!hasOverlayPermission) {
                            requestOverlayPermission(context)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                StatusPermissionChip(
                    drawableResId = R.drawable.ic_svg_tiktok_shortcut,
                    label = if (hasAccessibilityPermission) "Tự động vuốt: Đã bật" else "Trợ năng: Cần bật",
                    isGranted = hasAccessibilityPermission,
                    onClick = {
                        if (!hasAccessibilityPermission) {
                            requestAccessibilityPermission(context)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Android 13/14/15/16 Restricted Settings Unlock Guide (Matches Screenshot 2)
            if (!hasAccessibilityPermission) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldBadge),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restricted_settings_guide_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SvgDrawableGraphic(
                                drawableResId = R.drawable.ic_svg_check_circle,
                                tint = GoldBadge,
                                size = 16.dp
                            )
                            Text(
                                text = "Mở khóa 'Cài đặt bị hạn chế' (Nếu máy báo từ chối cấp quyền)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldBadge
                            )
                        }
                        Text(
                            text = "Khi vào Hỗ trợ tiếp cận nếu máy hiện 'Do chế độ Cài đặt hạn chế kiểm soát' hoặc 'Ứng dụng đã bị từ chối cấp quyền':\n1. Nhấn nút bên dưới để mở mục 'Thông tin ứng dụng TikStudio HD'.\n2. Nhìn góc trên cùng bên phải, bấm vào DẤU 3 CHẤM (⋮).\n3. Chọn 'Cho phép cài đặt bị hạn chế' -> Nhập vân tay hoặc mã PIN.\n4. Sau đó quay lại Hỗ trợ tiếp cận là sẽ gạt BẬT được công tắc!",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = { openAppInfoForRestrictedSettings(context) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldBadge,
                                contentColor = Color(0xFF0F172A)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bấm vào đây để mở Thông tin ứng dụng (Chọn dấu 3 chấm ⋮)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main CTA Buttons: Toggle Floating Shortcut & Launch TikTok
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleFloatingShortcut,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFloatingShortcutActive) EmeraldBadge else ElectricCyan,
                        contentColor = Color(0xFF001F24)
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_floating_shortcut_button")
                ) {
                    SvgDrawableGraphic(
                        drawableResId = if (isFloatingShortcutActive) R.drawable.ic_svg_check_circle else R.drawable.ic_svg_tiktok_shortcut,
                        tint = Color(0xFF001F24),
                        size = 16.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFloatingShortcutActive) "Đang bật phím tắt" else "Bật phím tắt TikTok",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    onClick = onLaunchTikTok,
                    shape = RoundedCornerShape(12.dp),
                    color = StudioBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("launch_tiktok_app_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgDrawableGraphic(
                            drawableResId = R.drawable.ic_svg_exit_bg,
                            tint = ElectricCyan,
                            size = 15.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mở app TikTok",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Actions: Hands-Free Auto-Scroll & Test Next Swipe (SVG Canvas graphics)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onToggleAutoScroll,
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAutoScrolling) EmeraldBadge.copy(alpha = 0.15f) else StudioBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAutoScrolling) EmeraldBadge else StudioCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_auto_scroll_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgPlayPauseGraphic(
                            isPlaying = isAutoScrolling,
                            color = if (isAutoScrolling) EmeraldBadge else TextPrimary,
                            size = 14.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isAutoScrolling) "Lướt (${autoScrollCountdown}s)" else "Lướt tự động",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAutoScrolling) EmeraldBadge else TextPrimary
                        )
                    }
                }

                Surface(
                    onClick = onTestSwipeNext,
                    shape = RoundedCornerShape(10.dp),
                    color = StudioBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_swipe_next_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SvgArrowForwardGraphic(
                            color = ElectricCyan,
                            size = 14.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Thử chuyển video",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mini Guide / How-To
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SvgDrawableGraphic(
                    drawableResId = R.drawable.ic_svg_tiktok_shortcut,
                    tint = GoldBadge,
                    size = 16.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cách dùng: Khi lướt TikTok, bấm nút nổi để tải video 1080p không logo và tự động chuyển sang video tiếp theo!",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPermissionChip(
    drawableResId: Int,
    label: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isGranted) EmeraldBadgeBg else Color(0x33FFAA00),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) EmeraldBadge.copy(alpha = 0.5f) else GoldBadge.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SvgDrawableGraphic(
                drawableResId = if (isGranted) R.drawable.ic_svg_check_circle else drawableResId,
                tint = if (isGranted) EmeraldBadge else GoldBadge,
                size = 13.dp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isGranted) EmeraldBadge else GoldBadge,
                maxLines = 1
            )
        }
    }
}

fun requestOverlayPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }
}

fun requestAccessibilityPermission(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {}
}

fun openAppInfoForRestrictedSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        requestAccessibilityPermission(context)
    }
}
