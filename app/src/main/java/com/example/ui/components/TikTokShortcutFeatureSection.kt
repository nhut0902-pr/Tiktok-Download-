package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    isFloatingShortcutActive: Boolean,
    isAccessibilityActive: Boolean,
    isAutoScrolling: Boolean,
    autoScrollCountdown: Int,
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
        // Header Row: Title & Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Phím tắt trong TikTok",
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
                                text = "MỚI",
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

        // Permission Status Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Overlay Permission Chip
            StatusPermissionChip(
                icon = Icons.Filled.Layers,
                label = if (hasOverlayPermission) "Bong bóng nổi: Đã cấp" else "Bong bóng: Cần cấp quyền",
                isGranted = hasOverlayPermission,
                onClick = {
                    if (!hasOverlayPermission) {
                        requestOverlayPermission(context)
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Accessibility Permission Chip
            StatusPermissionChip(
                icon = Icons.Filled.AccessibilityNew,
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
                Icon(
                    imageVector = if (isFloatingShortcutActive) Icons.Filled.CheckCircle else Icons.Filled.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
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
                    Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
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

        // Secondary Actions: Hands-Free Auto-Scroll & Test Next Swipe
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
                    Icon(
                        imageVector = if (isAutoScrolling) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (isAutoScrolling) EmeraldBadge else TextPrimary,
                        modifier = Modifier.size(15.dp)
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
            Icon(
                imageVector = Icons.Filled.TouchApp,
                contentDescription = null,
                tint = GoldBadge,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cách dùng: Khi lướt TikTok, bấm nút ⚡ nổi để tải video 1080p không logo và tự động chuyển sang video tiếp theo!",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun StatusPermissionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(
                imageVector = if (isGranted) Icons.Filled.CheckCircle else icon,
                contentDescription = null,
                tint = if (isGranted) EmeraldBadge else GoldBadge,
                modifier = Modifier.size(13.dp)
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
